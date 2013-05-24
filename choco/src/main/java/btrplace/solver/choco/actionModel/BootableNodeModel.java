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

import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.chocoUtil.FastImpliesEq;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.ElementV;
import choco.cp.solver.constraints.integer.TimesXYZ;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * Model an action that allows a node to be booted if necessary.
 * The model must provide an estimation of the action duration through a
 * {@link btrplace.solver.choco.durationEvaluator.DurationEvaluator} accessible from
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
 * <li>
 * The moment the node is powered up and down equals the moment the action starts and the moment it can not host VMs.
 * If the node is not powered up, these duration equals then 0
 * <ul>
 * <li>{@link #getPoweringStart()} = {@link #getStart()}</li>
 * <li>{@link #getPoweringEnd()} = {@link #getHostingEnd()}</li>
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

    private IntDomainVar start;

    private IntDomainVar end;

    private IntDomainVar isOnline;

    private IntDomainVar hostingStart;

    private IntDomainVar hostingEnd;

    private IntDomainVar effectiveDuration;

    private UUID node;

    /**
     * Make a new model.
     *
     * @param rp  the RP to use as a basis.
     * @param nId the node managed by the action
     * @throws SolverException if an error occurred
     */
    public BootableNodeModel(ReconfigurationProblem rp, UUID nId) throws SolverException {
        node = nId;

        int d = rp.getDurationEvaluators().evaluate(rp.getSourceModel(), BootNode.class, nId);
        CPSolver s = rp.getSolver();

        /*
            - If the node is hosting running VMs, it is necessarily online
            - If the node is offline, it is sure it cannot host any running VMs
        */
        isOnline = s.createBooleanVar(rp.makeVarLabel("bootableNode(", nId, ").online"));
        IntDomainVar isOffline = s.createBooleanVar(rp.makeVarLabel("bootableNode(", nId, ").offline"));
        s.post(s.neq(isOffline, isOnline));
        s.post(new FastImpliesEq(isOffline, rp.getNbRunningVMs()[rp.getNode(nId)], 0));


        /*
        * D = {0, d}
        * D = St * d;
        */
        effectiveDuration = s.createEnumIntVar(
                rp.makeVarLabel("bootableNode(", nId, ").effectiveDuration")
                , new int[]{0, d});
        s.post(new TimesXYZ(isOnline, s.makeConstantIntVar(d), effectiveDuration));

        /* As */
        start = rp.makeUnboundedDuration("bootableNode(", nId, ").start");
        /* Ae */
        end = rp.makeUnboundedDuration("bootableNode(", nId, ").end");
        s.post(s.leq(start, rp.getEnd()));
        s.post(s.leq(end, rp.getEnd()));
        /* Ae = As + D */
        s.post(s.eq(end, s.plus(start, effectiveDuration)));


        /* Hs = Ae */
        hostingStart = end;
        hostingEnd = rp.makeUnboundedDuration("bootableNode(", nId, ").hostingEnd");
        s.post(s.leq(hostingEnd, rp.getEnd()));


        /*
          T = { 0, RP.end}
          He = T[St]
         */
        s.post(new ElementV(new IntDomainVar[]{s.makeConstantIntVar(0), rp.getEnd(), isOnline, hostingEnd}, 0, s.getEnvironment()));
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        if (getState().getVal() == 1) {
            plan.add(new BootNode(node, start.getVal(), end.getVal()));
        }
        return true;
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
        return effectiveDuration;
    }

    @Override
    public UUID getNode() {
        return node;
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
        return start;
    }

    @Override
    public IntDomainVar getPoweringEnd() {
        return hostingEnd;
    }
}
