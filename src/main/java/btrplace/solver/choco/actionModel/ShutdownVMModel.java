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
import btrplace.plan.event.ShutdownVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import btrplace.solver.choco.VMActionModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.variables.integer.IntDomainVarAddCste;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * Model an action that stop a running VM.
 *
 * @author Fabien Hermenier
 */
public class ShutdownVMModel implements VMActionModel {

    private ReconfigurationProblem rp;

    private UUID vm;

    private IntDomainVar duration;

    private Slice cSlice;

    private IntDomainVar start;

    private IntDomainVar state;

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public ShutdownVMModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        this.rp = rp;
        this.vm = e;

        int d = rp.getDurationEvaluators().evaluate(ShutdownVM.class, e);
        assert d > 0;
        duration = rp.getSolver().createIntegerConstant(rp.makeVarLabel("shutdownVM(" + e + ").duration"), d);
        this.cSlice = new SliceBuilder(rp, e, "shutdownVM(" + e + ").cSlice").setHoster(rp.getCurrentVMLocation(rp.getVM(e)))
                .setEnd(rp.makeDuration(rp.makeVarLabel("shutdownVM(" + e + ").cSlice_end"), d, rp.getEnd().getSup()))
                .setExclusive(false)
                .build();
        CPSolver s = rp.getSolver();
        //s.post(s.geq(cSlice.getDuration(), d));
        start = new IntDomainVarAddCste(rp.getSolver(), "", cSlice.getEnd(), -d);
        state = rp.getSolver().makeConstantIntVar(0);
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        plan.add(new ShutdownVM(getVM(),
                rp.getSourceModel().getMapping().getVMLocation(getVM()),
                start.getVal(),
                cSlice.getEnd().getVal()));
        return true;
    }

    @Override
    public UUID getVM() {
        return vm;
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

    @Override
    public void visit(ActionModelVisitor v) {
        v.visit(this);
    }

}
