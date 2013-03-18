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
import btrplace.solver.choco.chocoUtil.ChocoUtils;
import btrplace.solver.choco.chocoUtil.FastIFFEq;
import btrplace.solver.choco.chocoUtil.FastImpliesEq;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * Model an action that allow a node to boot if necessary.
 *
 * @author Fabien Hermenier
 */
public class ShutdownableNodeModel implements NodeActionModel {

    private UUID node;

    private IntDomainVar isOnline, isOffline;

    private IntDomainVar duration;

    private IntDomainVar end;

    private IntDomainVar hostingStart;

    private IntDomainVar hostingEnd;

    private IntDomainVar start;

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the node managed by the action
     * @throws SolverException if an error occurred
     */
    public ShutdownableNodeModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        this.node = e;
        isOnline = rp.getSolver().createBooleanVar(rp.makeVarLabel(new StringBuilder("shutdownnableNode(").append(e).append(").online").toString()));

        CPSolver s = rp.getSolver();

        isOffline = s.createBooleanVar(rp.makeVarLabel(new StringBuilder("shutdownnableNode(").append(e).append(").offline").toString()));
        s.post(s.neq(isOnline, isOffline));
        //new BoolVarNot(s, rp.makeVarLabel("shutdownnableNode(" + e + ").offline"), (BooleanVarImpl) isOnline);

        int d = rp.getDurationEvaluators().evaluate(ShutdownNode.class, e);
        //Action duration is either 0 (no shutdown) or 'd' (shutdown)
        duration = s.createEnumIntVar(rp.makeVarLabel(new StringBuilder("shutdownableNode(").append(e).append(").duration").toString()), new int[]{0, d});

        //The node is already online, so it can host VMs at the beginning of the RP
        hostingStart = rp.getStart();
        //The moment the node can no longer host VMs varies depending on its next state
        hostingEnd = rp.makeDuration(new StringBuilder("shutdownableNode(").append(e).append(").hostingEnd").toString());

        //The duration between the moment the node can not host VMs anymore and the end of the RP:
        //online: hostingEnd == RP.end
        //offline: hostingEnd <= RP.end - duration, so that the node can be turned off asap.
        //duration = {O, K}
        s.post(s.leq(hostingEnd, CPSolver.minus(rp.getEnd(), duration)));
        s.post(new FastIFFEq(isOnline, duration, 0));

        start = rp.makeDuration(new StringBuilder("shutdownableNode(").append(e).append(").start").toString());
        s.post(s.eq(start, ChocoUtils.mult(s, isOffline, hostingEnd)));

        end = rp.makeDuration(new StringBuilder("shutdownableNode(").append(e).append(").end").toString());
        s.post(s.eq(end, s.plus(start, duration)));
        s.post(s.leq(duration, rp.getEnd()));
        s.post(s.leq(end, rp.getEnd()));
        s.post(s.leq(hostingStart, rp.getEnd()));
        s.post(s.leq(hostingEnd, rp.getEnd()));
        s.post(s.leq(start, rp.getEnd()));

        /**
         * If it is state to shutdown the node, then the duration of the dSlice is not null
         */
        s.post(new FastImpliesEq(isOffline, rp.getNbRunningVMs()[rp.getNode(e)], 0)); //Packing stuff; isOffline -> no VMs running
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
}
