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
import btrplace.solver.SolverException;
import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * A model for a running VM that stay online on the same node.
 */
public class StayRunningVMModel implements ActionModel {

    private Slice cSlice, dSlice;

    private ReconfigurationProblem rp;

    private UUID vm;

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public StayRunningVMModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        this.vm = e;
        this.rp = rp;
        IntDomainVar host = rp.makeCurrentHost(rp.makeVarLabel("stayRunningVM(" + e + ").host"), e);
        cSlice = new SliceBuilder(rp, e, "stayRunningVM(" + e + ").cSlice")
                .setHoster(host)
                .setExclusive(false)
                .build();
        dSlice = new SliceBuilder(rp, e, "stayRunningVM(" + e + ").dSlice")
                .setHoster(host)
                .setExclusive(false)
                .build();
        CPSolver s = rp.getSolver();
        s.post(s.eq(cSlice.getEnd(), dSlice.getStart()));
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        int st = dSlice.getStart().getVal();
        UUID src = rp.getNode(cSlice.getHoster().getVal());
        rp.insertAllocateAction(plan, vm, src, st, st);
        return true;
    }

    @Override
    public IntDomainVar getStart() {
        return rp.getStart();
    }

    @Override
    public IntDomainVar getEnd() {
        return rp.getStart();
    }

    @Override
    public IntDomainVar getDuration() {
        return rp.getStart();
    }

    @Override
    public Slice getCSlice() {
        return cSlice;
    }

    @Override
    public Slice getDSlice() {
        return dSlice;
    }

    @Override
    public IntDomainVar getState() {
        return null;
    }

    /**
     * Get the VM manipulated by the action.
     *
     * @return the VM identifier
     */
    public UUID getVM() {
        return vm;
    }

    @Override
    public void visit(ActionModelVisitor v) {
        v.visit(this);
    }

}
