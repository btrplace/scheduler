/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.ShutdownNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.NodeActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.chocoUtil.FastImpliesEq;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.ElementV;
import choco.cp.solver.constraints.integer.channeling.BooleanChanneling;
import choco.cp.solver.variables.integer.BoolVarNot;
import choco.cp.solver.variables.integer.BooleanVarImpl;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * Model an action that allow a node to boot if necessary.
 * The model must provide an estimation of the action duration through a
 * {@link btrplace.solver.choco.DurationEvaluator} accessible from
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
 * <li>
 * The moment the node is powered up equals the moment the reconfiguration starts as the node is already online.
 * The moment the node is powered down equals the moment the node can no longer host VMs plus the action duration.
 * <ul>
 * <li>{@link #getPoweringStart()} = {@link #getStart()}</li>
 * <li>{@link #getPoweringEnd()} = {@link #getHostingEnd()} + {@link #getDuration()}</li>
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

    private UUID node;

    private IntDomainVar isOnline, isOffline;

    private IntDomainVar duration;

    private IntDomainVar end;

    private IntDomainVar hostingStart;

    private IntDomainVar hostingEnd;

    private IntDomainVar start;

    private IntDomainVar powerStart;

    private IntDomainVar powerEnd;

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the node managed by the action
     * @throws SolverException if an error occurred
     */
    public ShutdownableNodeModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        this.node = e;


        CPSolver s = rp.getSolver();

        /*
            - If the node is hosting running VMs, it is necessarily online
            - If the node is offline, it is sure it cannot host any running VMs
        */
        isOnline = s.createBooleanVar(rp.makeVarLabel("shutdownableNode(", e, ").online"));
        isOffline = new BoolVarNot(s, rp.makeVarLabel("shutdownableNode(", e, ").offline"), (BooleanVarImpl) isOnline);
        s.post(new FastImpliesEq(isOffline, rp.getNbRunningVMs()[rp.getNode(e)], 0));

        /*
        * D = {0, d}
        * D = St * d;
        */
        int d = rp.getDurationEvaluators().evaluate(ShutdownNode.class, e);
        duration = s.createEnumIntVar(rp.makeVarLabel("shutdownableNode(", e, ").duration"), new int[]{0, d});
        s.post(new BooleanChanneling(isOnline, duration, 0));

        //The moment of shutdown action consume
        /* As */
        start = rp.makeUnboundedDuration("shutdownableNode(", e, ").start");
        //The moment of shutdown action end
        /* Ae */
        end = rp.makeUnboundedDuration("shutdownableNode(", e, ").end");
        s.post(s.leq(end, rp.getEnd()));
        s.post(s.leq(start, rp.getEnd()));
        /* Ae = As + D */
        s.post(s.eq(end, s.plus(start, duration)));

        //The node is already online, so it can host VMs at the beginning of the RP
        hostingStart = rp.getStart();
        //The moment the node can no longer host VMs varies depending on its next state
        hostingEnd = rp.makeUnboundedDuration("shutdownableNode(", e, ").hostingEnd");
        s.post(s.leq(hostingEnd, rp.getEnd()));

        /*
          T = { As, RP.end}
          He = T[St]
         */
        s.post(new ElementV(new IntDomainVar[]{start, rp.getEnd(), isOnline, hostingEnd}, 0, s.getEnvironment()));


        //The node is already online, so it starts at the beginning of the RP
        powerStart = rp.getStart();
        //The moment the node is offline. It depends on the hosting end time and the duration of the shutdown action
        powerEnd = rp.makeUnboundedDuration("shutdownableNode(", e, ").powerEnd");
        s.post(s.eq(powerEnd, s.plus(hostingEnd, duration)));
    }


    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        if (isOffline.getVal() == 1) {
            plan.add(new ShutdownNode(node, hostingEnd.getVal(), end.getVal()));
        }
        return true;
    }

    @Override
    public UUID getNode() {
        return node;
    }

    @Override
    public IntDomainVar getStart() {
        return start;
    }

    @Override
    public IntDomainVar getEnd() {
        return end;
    }

    @Override
    public IntDomainVar getDuration() {
        return duration;
    }

    @Override
    public IntDomainVar getState() {
        return isOnline;
    }

    @Override
    public void visit(ActionModelVisitor v) {
        v.visit(this);
    }

    @Override
    public IntDomainVar getHostingStart() {
        return hostingStart;
    }

    @Override
    public IntDomainVar getHostingEnd() {
        return hostingEnd;
    }

    @Override
    public IntDomainVar getPoweringStart() {
        return powerStart;
    }

    @Override
    public IntDomainVar getPoweringEnd() {
        return powerEnd;
    }
}
