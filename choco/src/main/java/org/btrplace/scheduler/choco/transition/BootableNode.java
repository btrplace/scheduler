/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.Node;
import org.btrplace.model.NodeState;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.BootNode;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.extensions.FastImpliesEq;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;


/**
 * Model a transition that allows an offline node to be booted if necessary.
 * An estimation of the action duration must be provided through a
 * {@link org.btrplace.scheduler.choco.duration.ActionDurationEvaluator} accessible from
 * {@link org.btrplace.scheduler.choco.ReconfigurationProblem#getDurationEvaluators()} with the key {@code BootNode.class}
 * <p>
 * The action is modeled as follow:
 * <p>
 * <ul>
 * <li>Definition of the node state. If the node is offline, then no VMs can run on it:
 * <ul>
 * <li>{@link #getState()} = {0,1}</li>
 * <li>{@link #getState()} = 0 -> {@code btrplace.solver.choco.ReconfigurationProblem.getNbRunningVMs()[nIdx] = 0}</li>
 * </ul>
 * </li>
 * <li>The action duration equals 0 if the node stays offline. Otherwise, it equals the evaluated action duration {@code d}
 * retrieved from {@link org.btrplace.scheduler.choco.ReconfigurationProblem#getDurationEvaluators()}:
 * <ul>
 * <li>{@link #getDuration()} = {0,d}</li>
 * <li>{@link #getDuration()} = {@link #getState()} * d</li>
 * </ul>
 * </li>
 * <li>The action starts and ends necessarily before the end of the reconfiguration problem. Their difference
 * equals the action duration. If the node stays offline then the action starts and ends at moment 0.
 * <ul>
 * <li>{@link #getStart()} < {@link org.btrplace.scheduler.choco.ReconfigurationProblem#getEnd()}</li>
 * <li>{@link #getEnd()} < {@link org.btrplace.scheduler.choco.ReconfigurationProblem#getEnd()}</li>
 * <li>{@link #getEnd()} = {@link #getStart()} + {@link #getDuration()}</li>
 * </ul>
 * </li>
 * <li>The node can consume hosting VMs and the end of the action. If the node goes online, it can stop hosting VMs at
 * the end of the reconfiguration. Otherwise, it is never capable of hosting VMs (the deadline equals 0)
 * <ul>
 * <li>{@link #getHostingStart()} = {@link #getEnd()}</li>
 * <li>{@code T} = { {@code 0}, {@link org.btrplace.scheduler.choco.ReconfigurationProblem#getEnd()} }; {@link #getHostingEnd()} = T[{@link #getState()}]</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * If the reconfiguration problem has a solution, a {@link org.btrplace.plan.event.BootNode} action
 * is inserted into the resulting reconfiguration plan iff the node has to be turned online.
 *
 * @author Fabien Hermenier
 */
public class BootableNode implements NodeTransition {

    public static final String PREFIX = "bootableNode(";
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
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred
     */
    public BootableNode(ReconfigurationProblem rp, Node nId) throws SchedulerException {
        node = nId;

        int d = rp.getDurationEvaluators().evaluate(rp.getSourceModel(), BootNode.class, nId);
        Solver s = rp.getSolver();

        /*
            - If the node is hosting running VMs, it is necessarily online
            - If the node is offline, it is sure it cannot host any running VMs
        */
        isOnline = VariableFactory.bool(rp.makeVarLabel(PREFIX, nId, ").online"), s);
        BoolVar isOffline = VariableFactory.not(isOnline);
        s.post(new FastImpliesEq(isOffline, rp.getNbRunningVMs()[rp.getNode(nId)], 0));


        /*
        * D = {0, d}
        * D = St * d;
        */
        effectiveDuration = VariableFactory.enumerated(
                rp.makeVarLabel(PREFIX, nId, ").effectiveDuration")
                , new int[]{0, d}, s);
        s.post(IntConstraintFactory.times(isOnline, VariableFactory.fixed(d, s), effectiveDuration));

        /* As */
        start = rp.makeUnboundedDuration(PREFIX, nId, ").start");
        /* Ae */
        end = rp.makeUnboundedDuration(PREFIX, nId, ").end");

        s.post(IntConstraintFactory.arithm(start, "<=", rp.getEnd()));

        s.post(IntConstraintFactory.arithm(end, "<=", rp.getEnd()));
        /* Ae = As + D */
        /*Task t = */
        VariableFactory.task(start, effectiveDuration, end);

        /* Hs = Ae */
        hostingStart = end;
        hostingEnd = rp.makeUnboundedDuration(PREFIX, nId, ").hostingEnd");
        s.post(IntConstraintFactory.arithm(hostingEnd, "<=", rp.getEnd()));

        /*
          T = { 0, RP.end}
          He = T[St]
         */
        s.post(IntConstraintFactory.element(hostingEnd, new IntVar[]{rp.getStart(), rp.getEnd()}, isOnline, 0));
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
    public BoolVar getState() {
        return isOnline;
    }

    @Override
    public IntVar getHostingStart() {
        return hostingStart;
    }

    @Override
    public IntVar getHostingEnd() {
        return hostingEnd;
    }

    /**
     * The builder devoted to a offline->(online|offline) transition.
     */
    public static class Builder extends NodeTransitionBuilder {

        /**
         * New builder.
         */
        public Builder() {
            super("bootable", NodeState.OFFLINE);
        }

        @Override
        public NodeTransition build(ReconfigurationProblem r, Node n) throws SchedulerException {
            return new BootableNode(r, n);
        }
    }
}
