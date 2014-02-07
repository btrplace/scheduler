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
import btrplace.plan.event.BootNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.chocoUtil.FastImpliesEq;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.Task;
import solver.variables.VariableFactory;


/**
 * Model an action that allows a node to be booted if necessary.
 * The model must provide an estimation of the action duration through a
 * {@link btrplace.solver.choco.durationEvaluator.ActionDurationEvaluator} accessible from
 * {@link btrplace.solver.choco.ReconfigurationProblem#getDurationEvaluators()} with the key {@code BootNode.class}
 * <p/>
 * The action is modeled as follow:
 * <p/>
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
 * equals the action duration. If the node stays offline then the action starts and ends at moment 0.
 * <ul>
 * <li>{@link #getStart()} < {@link btrplace.solver.choco.ReconfigurationProblem#getEnd()}</li>
 * <li>{@link #getEnd()} < {@link btrplace.solver.choco.ReconfigurationProblem#getEnd()}</li>
 * <li>{@link #getEnd()} = {@link #getStart()} + {@link #getDuration()}</li>
 * </ul>
 * </li>
 * <li>The node can consume hosting VMs and the end of the action. If the node goes online, it can stop hosting VMs at
 * the end of the reconfiguration. Otherwise, it is never capable of hosting VMs (the deadline equals 0)
 * <ul>
 * <li>{@link #getHostingStart()} = {@link #getEnd()}</li>
 * <li>{@code T} = { {@code 0}, {@link btrplace.solver.choco.ReconfigurationProblem#getEnd()} }; {@link #getHostingEnd()} = T[{@link #getState()}]</li>
 * </ul>
 * </li>
 * </ul>
 * <p/>
 * If the reconfiguration problem has a solution, a {@link btrplace.plan.event.BootNode} action
 * is inserted into the resulting reconfiguration plan iff the node has to be turned online.
 *
 * @author Fabien Hermenier
 */
public class BootableNodeModel implements NodeActionModel {

    private IntVar start;

    private IntVar end;

    private BoolVar isOnline;

    private IntVar hostingStart;

    private IntVar hostingEnd;

    private IntVar effectiveDuration;

    private Node node;

    /**
     * Make a new model.
     *
     * @param rp  the RP to use as a basis.
     * @param nId the node managed by the action
     * @throws SolverException if an error occurred
     */
    public BootableNodeModel(ReconfigurationProblem rp, Node nId) throws SolverException {
        node = nId;

        int d = rp.getDurationEvaluators().evaluate(rp.getSourceModel(), BootNode.class, nId);
        Solver s = rp.getSolver();

        /*
            - If the node is hosting running VMs, it is necessarily online
            - If the node is offline, it is sure it cannot host any running VMs
        */
        isOnline = VariableFactory.bool(rp.makeVarLabel("bootableNode(", nId, ").online"), s);
        IntVar isOffline = VariableFactory.not(isOnline);//VariableFactory.bool(rp.makeVarLabel("bootableNode(", nId, ").offline"), s);
        //s.post(s.neq(isOffline, isOnline));
        s.post(new FastImpliesEq(isOffline, rp.getNbRunningVMs()[rp.getNode(nId)], 0));


        /*
        * D = {0, d}
        * D = St * d;
        */
        effectiveDuration = VariableFactory.enumerated(
                rp.makeVarLabel("bootableNode(", nId, ").effectiveDuration")
                , new int[]{0, d}, s);
        s.post(IntConstraintFactory.times(isOnline, VariableFactory.fixed(d, s), effectiveDuration));
        //s.post(new TimesXYZ(isOnline, VariableFactory.fixed(d, s), effectiveDuration));

        /* As */
        start = rp.makeUnboundedDuration("bootableNode(", nId, ").start");
        /* Ae */
        end = rp.makeUnboundedDuration("bootableNode(", nId, ").end");

        s.post(IntConstraintFactory.arithm(start, "<=", rp.getEnd()));//s.leq(start, rp.getEnd()));
        //s.post(s.leq(end, rp.getEnd()));
        s.post(IntConstraintFactory.arithm(end, "<=", rp.getEnd()));
        /* Ae = As + D */
        Task t = VariableFactory.task(start, effectiveDuration, end);
        //s.post(s.eq(end, s.plus(start, effectiveDuration)));


        /* Hs = Ae */
        hostingStart = end;
        hostingEnd = rp.makeUnboundedDuration("bootableNode(", nId, ").hostingEnd");
        //s.post(s.leq(hostingEnd, rp.getEnd()));
        s.post(IntConstraintFactory.arithm(hostingEnd, "<=", rp.getEnd()));


        /*
          T = { 0, RP.end}
          He = T[St]
         */
        s.post(new ElementV(new IntVar[]{VariableFactory.zero(s), rp.getEnd(), isOnline, hostingEnd}, 0, s.getEnvironment()));
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        if (getState().getValue() == 1) {
            plan.add(new BootNode(node, start.getValue(), end.getValue()));
        }
        return true;
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
        return effectiveDuration;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public IntVar getState() {
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
