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
import btrplace.solver.choco.chocoUtil.FastImpliesEq;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.TimesXYZ;
import choco.cp.solver.variables.integer.IntDomainVarAddCste;
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
        isOnline = rp.getSolver().createBooleanVar(rp.makeVarLabel("shutdownnableNode(" + e + ").online"));

        CPSolver s = rp.getSolver();

        isOffline = s.createBooleanVar(rp.makeVarLabel("shutdownnableNode(" + e + ").offline"));
        s.post(s.neq(isOnline, isOffline));
        //new BoolVarNot(s, rp.makeVarLabel("shutdownnableNode(" + e + ").offline"), (BooleanVarImpl) isOnline);

        int d = rp.getDurationEvaluators().evaluate(ShutdownNode.class, e);
        //Duration is either 0 (no shutdown) or 'd' (shutdown)
        duration = s.createEnumIntVar(rp.makeVarLabel("shutdownableNode(" + e + ").duration"), new int[]{0, d});

        hostingStart = rp.getStart();
        hostingEnd = rp.makeDuration("shutdownableNode(" + e + ").hostingEnd");
        s.eq(hostingEnd, s.plus(rp.getEnd(), ChocoUtils.mult(s, isOffline, -d)));
        start = rp.makeDuration(rp.makeVarLabel("shutdownableNode(" + e + ").start"));
        s.post(s.eq(start, ChocoUtils.mult(s, isOffline, hostingEnd)));

        end = rp.makeDuration(rp.makeVarLabel("shutdownableNode(" + e + ").end"));
        IntDomainVar endIfGoesOffline = new IntDomainVarAddCste(s, rp.makeVarLabel("endIfOffline"), hostingEnd, d);
        s.post(new TimesXYZ(isOffline, endIfGoesOffline, end));
        s.post(s.leq(duration, rp.getEnd()));
        s.post(s.leq(end, rp.getEnd()));
        s.post(s.leq(hostingStart, rp.getEnd()));
        s.post(s.leq(hostingEnd, rp.getEnd()));
        s.post(s.leq(start, rp.getEnd()));
        /**
         * If it is state to shutdown the node, then the duration of the dSlice is not null
         */
        s.post(new FastImpliesEq(isOffline, rp.getVMsCountOnNodes()[rp.getNode(e)], 0)); //Packing stuff; isOffline -> no VMs running
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
