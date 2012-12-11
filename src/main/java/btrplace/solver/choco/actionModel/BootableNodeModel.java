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
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import btrplace.solver.choco.chocoUtil.FastIFFEq;
import btrplace.solver.choco.chocoUtil.FastImpliesEq;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.reified.ReifiedFactory;
import choco.cp.solver.variables.integer.IntDomainVarAddCste;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * Model an action that allow a node to be booted if necessary.
 *
 * @author Fabien Hermenier
 */
public class BootableNodeModel implements NodeActionModel {

    private Slice cSlice;

    private IntDomainVar duration;

    private IntDomainVar start;

    private IntDomainVar state;

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
        int nIdx = rp.getNode(nId);
        CPSolver s = rp.getSolver();

        //TODO: makes it consume all the resources of the node
        int d = rp.getDurationEvaluators().evaluate(BootNode.class, nId);
        duration = s.createEnumIntVar(rp.makeVarLabel("bootableNode.duration(" + nId + ")"), new int[]{0, d});

        cSlice = new SliceBuilder(rp, nId, "bootableNode(" + nId + ").cSlice")
                .setEnd(rp.makeDuration(rp.makeVarLabel("bootableNode(" + nId + ").cSlice_end")))
                .setHoster(nIdx)
                .build();

        start = new IntDomainVarAddCste(s, rp.makeVarLabel("bootableNode(" + nId + ").start"), cSlice.getEnd(), -d);
        //Unknown state
        state = s.createBooleanVar(rp.makeVarLabel("bootableNode(" + nId + ").state"));

        //the node goes online <-> duration == d
        s.post(new FastIFFEq(state, duration, d));
        s.post(s.leq(duration, cSlice.getEnd()));
        /**
         * used denotes whether or not the node is used, \ie it host running VMs
         */
        IntDomainVar used = s.createBooleanVar(rp.makeVarLabel("bootableNode_isUsed(" + nId + ")"));
        s.post(ReifiedFactory.builder(used, s.neq(rp.getVMsCountOnNodes()[nIdx], 0), s));
        s.post(new FastImpliesEq(used, state, 1));
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        if (start.getVal() == 1) {
            plan.add(new BootNode(node, start.getVal(), getEnd().getVal()));
        }
        return true;
    }

    @Override
    public IntDomainVar getStart() {
        return start;
    }

    @Override
    public IntDomainVar getEnd() {
        return cSlice.getEnd();
    }

    @Override
    public IntDomainVar getDuration() {
        return duration;
    }

    @Override
    public Slice getCSlice() {
        return cSlice;
    }

    @Override
    public Slice getDSlice() {
        return null;
    }

    @Override
    public UUID getNode() {
        return node;
    }

    @Override
    public IntDomainVar getState() {
        return state;
    }

    @Override
    public void visit(ActionModelVisitor v) {
        v.visit(this);
    }
}
