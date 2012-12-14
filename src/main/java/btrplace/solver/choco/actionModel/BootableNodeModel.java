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
import btrplace.plan.event.BootNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.NodeActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.chocoUtil.FastImpliesEq;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.ElementV;
import choco.cp.solver.constraints.integer.TimesXYZ;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * Model an action that allow a node to be booted if necessary.
 *
 * @author Fabien Hermenier
 */
public class BootableNodeModel implements NodeActionModel {

    private IntDomainVar start;

    private IntDomainVar end;

    private IntDomainVar isOnline;

    private IntDomainVar hostingStart;

    private IntDomainVar hostingEnd;

    private IntDomainVar duration;

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

        int d = rp.getDurationEvaluators().evaluate(BootNode.class, nId);
        CPSolver s = rp.getSolver();

        isOnline = s.createBooleanVar(rp.makeVarLabel("bootableNode(" + nId + ").online"));
        IntDomainVar isOffline = s.createBooleanVar(rp.makeVarLabel("bootableNode(" + nId + ").offline"));
        s.post(s.neq(isOffline, isOnline));
        s.post(new FastImpliesEq(isOffline, rp.getVMsCountOnNodes()[rp.getNode(nId)], 0));
        start = rp.getStart();
        end = rp.makeDuration(rp.makeVarLabel("bootableNode(" + nId + ").end"));

        hostingStart = rp.makeDuration(rp.makeVarLabel("bootableNode(" + nId + ").hostingStart"));
        s.post(new TimesXYZ(isOnline, hostingStart, end));
        //s.post(new TimesXYZ(isOffline, rp.getEnd(), hostingStart));
        s.post(s.leq(hostingEnd, rp.getEnd()));
        s.post(s.leq(end, rp.getEnd()));
        s.post(s.leq(hostingStart, rp.getEnd()));
        IntDomainVar cDur = s.makeConstantIntVar(d);

        /**
         * if isOnline == 0, hostingStart = cDur , the boot duration
         * else            , hostingStart = rp.getEnd(), so no dSlice
         */
        s.post(new ElementV(new IntDomainVar[]{rp.getEnd(), cDur, isOnline, hostingStart}, 0, s.getEnvironment()));
        hostingEnd = rp.getEnd();
        duration = rp.makeDuration(rp.makeVarLabel("bootableNode(" + nId + ").duration"));
        s.post(s.eq(duration, s.minus(end, start)));
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
        return duration;
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
}
