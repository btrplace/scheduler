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
import btrplace.plan.action.ForgeVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import choco.cp.solver.CPSolver;
import choco.cp.solver.variables.integer.IntDomainVarAddCste;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * Model an action that forge a VM to put it into the ready state.
 *
 * @author Fabien Hermenier
 */
public class ForgeVMModel implements ActionModel {

    private UUID vm;

    private IntDomainVar duration;

    private IntDomainVar state;

    private IntDomainVar end;

    private Slice dSlice;

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public ForgeVMModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        /*
         * We don't make any "real" dslice cause it may impacts the TaskScheduler
         */
        int d = rp.getDurationEvaluators().evaluate(ForgeVM.class, e);
        CPSolver s = rp.getSolver();
        duration = s.makeConstantIntVar(d);
        state = s.makeConstantIntVar(0);
        vm = e;

        dSlice = new SliceBuilder(rp, e, rp.makeVarLabel("forge(" + e + ").dSlice"))
                .setExclusive(false)
                .setEnd(rp.getEnd())
                .setStart(rp.makeDuration(rp.makeVarLabel("forge(" + e + ").start")))
                .build();
        s.post(s.leq(d, dSlice.getDuration()));
        end = new IntDomainVarAddCste(s, rp.makeVarLabel("forge(" + e + ").start"), dSlice.getStart(), -d);
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        return true;
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
    public IntDomainVar getStart() {
        return dSlice.getStart();
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
    public Slice getCSlice() {
        return null;
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
