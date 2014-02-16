/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.actionModel;

import btrplace.model.Node;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.ShutdownNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.chocoUtil.FastIFFEq;
import btrplace.solver.choco.chocoUtil.FastImpliesEq;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.Task;
import solver.variables.VariableFactory;


/**
 * Model an action that allow a node to boot if necessary.
 * The model must provide an estimation of the action duration through a
 * {@link btrplace.solver.choco.durationEvaluator.ActionDurationEvaluator} accessible from
 * {@link btrplace.solver.choco.ReconfigurationProblem#getDurationEvaluators()} with the key {@code ShutdownNode.class}
 * <p/>
 * The action is modeled as follow:
 * <ul>
 * <li>Definition of the node state. If the node is offline, then no VMs can run on it:
 * <ul>
 * <li>{@link #getState()} = {0,1}</li>
 * <li>{@link #getState()} = 0 -> {@code btrplace.solver.choco.ReconfigurationProblem.getNbRunningVMs()[nIdx] = 0}</li>
 * </ul>
 * </li>
 * <li>The action duration equals 0 if the node stays offline. Otherwise, it equals the evaluated action duration {@code d}
 * retrieved from {@link btrplace.solver.choco.ReconfigurationProblem#getDurationEvaluators()}:
 * <ul>
 * <li>{@link #getDuration()} = {0,d}</li>
 * <li>{@link #getDuration()} = {@link #getState()} * d</li>
 * </ul>
 * </li>
 * <li>The action starts and ends necessarily before the end of the reconfiguration problem. Their difference
 * equals the action duration. If the node stays online then the action starts and ends at moment 0.
 * <ul>
 * <li>{@link #getStart()} < {@link btrplace.solver.choco.ReconfigurationProblem#getEnd()}</li>
 * <li>{@link #getEnd()} < {@link btrplace.solver.choco.ReconfigurationProblem#getEnd()}</li>
 * <li>{@link #getEnd()} = {@link #getStart()} + {@link #getDuration()}</li>
 * </ul>
 * </li>
 * <li>The node can consume hosting VMs and the beginning of the reconfiguration plan. If the node goes offline, it stops hosting VMs at
 * the beginning of the action. Otherwise, it equals the end of the reconfiguration process so that it is always capable
 * of hosting VMs.
 * <ul>
 * <li>{@link #getHostingStart()} = {@link btrplace.solver.choco.ReconfigurationProblem#getStart()}</li>
 * <li>{@code T} = { {@link #getStart()}, {@link btrplace.solver.choco.ReconfigurationProblem#getEnd()} }; {@link #getHostingEnd()} = T[{@link #getState()}]</li>
 * </ul>
 * </li>
 * </ul>
 * <p/>
 * If the reconfiguration problem has a solution, a {@link btrplace.plan.event.ShutdownNode} action is inserted
 * into the resulting reconfiguration plan if the node is turned offline.
 *
 * @author Fabien Hermenier, Tu Dang
 */
public class ShutdownableNodeModel implements NodeActionModel {

    private Node node;

    private BoolVar isOnline, isOffline;

    private IntVar duration;

    private IntVar end;

    private IntVar hostingStart;

    private IntVar hostingEnd;

    private IntVar start;

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the node managed by the action
     * @throws SolverException if an error occurred
     */
    public ShutdownableNodeModel(ReconfigurationProblem rp, Node e) throws SolverException {
        this.node = e;


        Solver s = rp.getSolver();

        /*
            - If the node is hosting running VMs, it is necessarily online
            - If the node is offline, it is sure it cannot host any running VMs
        */
        isOnline = VariableFactory.bool(rp.makeVarLabel("shutdownableNode(", e, ").online"), rp.getSolver());
        isOffline = VariableFactory.not(isOnline);//new BoolVarNot(s, rp.makeVarLabel("shutdownableNode(", e, ").offline"), (BooleanVarImpl) isOnline);
        s.post(new FastImpliesEq(isOffline, rp.getNbRunningVMs()[rp.getNode(e)], 0));

        /*
        * D = {0, d}
        * D = St * d;
        */
        int d = rp.getDurationEvaluators().evaluate(rp.getSourceModel(), ShutdownNode.class, e);
        duration = VariableFactory.enumerated(rp.makeVarLabel("shutdownableNode(", e, ").duration"), new int[]{0, d}, rp.getSolver());
        s.post(new FastIFFEq(isOnline, duration, 0));
        //s.post(new BooleanChanneling(isOnline, duration, 0));

        //The moment of shutdown action consume
        /* As */
        start = rp.makeUnboundedDuration("shutdownableNode(", e, ").start");
        //The moment of shutdown action end
        /* Ae */
        end = rp.makeUnboundedDuration("shutdownableNode(", e, ").end");
        //s.post(s.leq(end, rp.getEnd()));
        s.post(IntConstraintFactory.arithm(end, "<=", rp.getEnd()));
        //s.post(s.leq(start, rp.getEnd()));
        s.post(IntConstraintFactory.arithm(start, "<=", rp.getEnd()));
        s.post(IntConstraintFactory.arithm(duration, "<=", rp.getEnd()));
        /* Ae = As + D */
        Task t = VariableFactory.task(start, duration, end);
        //s.post(s.eq(end, s.plus(start, duration)));

        //The node is already online, so it can host VMs at the beginning of the RP
        hostingStart = rp.getStart();
        //The moment the node can no longer host VMs varies depending on its next state
        hostingEnd = rp.makeUnboundedDuration("shutdownableNode(", e, ").hostingEnd");
        //s.post(s.leq(hostingEnd, rp.getEnd()));
        s.post(IntConstraintFactory.arithm(hostingEnd, "<=", rp.getEnd()));

        /*
          T = { As, RP.end}
          He = T[St]
         */
        //s.post(IntConstraintFactory.element())
        s.post(IntConstraintFactory.element(hostingEnd, new IntVar[]{start, rp.getEnd()}, isOnline, 0));
        //s.post(new Element(new IntVar[]{start, rp.getEnd(), isOnline, hostingEnd}, 0, s.getEnvironment()));
    }


    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        if (isOffline.getValue() == 1) {
            plan.add(new ShutdownNode(node, hostingEnd.getValue(), end.getValue()));
        }
        return true;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public IntVar getStart() {
        return start;
    }

    @Override
    public IntVar getEnd() {
        return end;
    }

    @Override
    public IntVar getDuration() {
        return duration;
    }

    @Override
    public BoolVar getState() {
        return isOnline;
    }

    @Override
    public void visit(ActionModelVisitor v) {
        v.visit(this);
    }

    @Override
    public IntVar getHostingStart() {
        return hostingStart;
    }

    @Override
    public IntVar getHostingEnd() {
        return hostingEnd;
    }
}
