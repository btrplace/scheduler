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

import btrplace.plan.Action;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.SuspendVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import choco.cp.solver.variables.integer.IntDomainVarAddCste;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model an action where a running VM goes into the sleeping state through a {@link SuspendVM} action.
 *
 * @author Fabien Hermenier
 */
public class SuspendVMModel implements ActionModel {

    private Slice cSlice;

    private IntDomainVar start;

    private IntDomainVar duration;

    private UUID vm;

    private ReconfigurationProblem rp;

    private IntDomainVar state;

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public SuspendVMModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        this.rp = rp;
        this.vm = e;

        int d = rp.getDurationEvaluators().evaluate(SuspendVM.class, e);

        duration = rp.makeDuration(rp.makeVarLabel("suspendVM(" + e + ").duration"), d, d);
        this.cSlice = new SliceBuilder(rp, e, "suspendVM(" + e + ").cSlice").setHoster(rp.getCurrentVMLocation(rp.getVM(e)))
                .setEnd(rp.makeDuration(rp.makeVarLabel("suspendVM(" + e + ").cSlice_end"), d, rp.getEnd().getSup()))
                .setExclusive(false)
                .build();
        start = new IntDomainVarAddCste(rp.getSolver(), rp.makeVarLabel("suspendVM(" + e + ").start"), cSlice.getEnd(), -d);
        state = rp.getSolver().makeConstantIntVar(0);
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        List<Action> a = new ArrayList<Action>();
        UUID node = rp.getNode(cSlice.getHoster().getVal());
        plan.add(new SuspendVM(vm, node, node, start.getVal(), getEnd().getVal()));
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
    public IntDomainVar getState() {
        return state;
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
