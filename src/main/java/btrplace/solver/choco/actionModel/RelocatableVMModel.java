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
import btrplace.plan.event.MigrateVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import btrplace.solver.choco.VMActionModel;
import btrplace.solver.choco.chocoUtil.FastIFFEq;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.reified.ReifiedFactory;
import choco.cp.solver.variables.integer.BoolVarNot;
import choco.cp.solver.variables.integer.BooleanVarImpl;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * Model an action that allow a VM to be migrated if necessary.
 * TODO: Integrate re-instantiable experiments
 *
 * @author Fabien Hermenier
 */
public class RelocatableVMModel implements VMActionModel {

    private Slice cSlice, dSlice;

    private ReconfigurationProblem rp;

    private UUID vm;

    private IntDomainVar state;

    private IntDomainVar duration;


    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public RelocatableVMModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        this.vm = e;
        this.rp = rp;

        int d = rp.getDurationEvaluators().evaluate(MigrateVM.class, e);

        CPSolver s = rp.getSolver();
        duration = s.createEnumIntVar(rp.makeVarLabel("relocatable(" + e + ").duration"), new int[]{0, d});
        cSlice = new SliceBuilder(rp, e, "relocatable(" + e + ").cSlice")
                .setHoster(rp.getNode(rp.getSourceModel().getMapping().getVMLocation(e)))
                .setEnd(rp.makeDuration(rp.makeVarLabel("relocatable(" + e + ").cSlice_end")))
                .build();

        dSlice = new SliceBuilder(rp, e, "relocatable(" + e + ").dSlice")
                .setStart(rp.makeDuration(rp.makeVarLabel("relocatable(" + e + ").dSlice_start")))
                .build();
        IntDomainVar move = s.createBooleanVar(rp.makeVarLabel("relocatable(" + e + ").move"));
        s.post(ReifiedFactory.builder(move, s.neq(cSlice.getHoster(), dSlice.getHoster()), s));

        IntDomainVar stay = new BoolVarNot(s, rp.makeVarLabel("relocatable(" + e + ").stay"), (BooleanVarImpl) move);

        s.post(new FastIFFEq(stay, duration, 0));

        /*boolean increase = false;
        if (!increase) {
            s.post(new FastImpliesEq(stay, cSlice.getDuration(), 0));
        } else {
            s.post(new FastImpliesEq(stay, dSlice.getDuration(), 0));
        } */
        s.post(s.leq(duration, cSlice.getDuration()));
        s.post(s.leq(duration, dSlice.getDuration()));
        s.post(s.eq(cSlice.getEnd(), s.plus(dSlice.getStart(), duration)));

        s.post(s.leq(cSlice.getDuration(), rp.getEnd()));
        s.post(s.leq(dSlice.getDuration(), rp.getEnd()));

        state = s.makeConstantIntVar(1);
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        UUID src = rp.getNode(cSlice.getHoster().getVal());
        if (cSlice.getHoster().getVal() != dSlice.getHoster().getVal()) {
            UUID dst = rp.getNode(dSlice.getHoster().getVal());
            int st = getStart().getVal();
            int ed = getEnd().getVal();
            MigrateVM a = new MigrateVM(vm, src, dst, st, ed);
            rp.insertNotifyAllocations(a, vm, Action.Hook.post);
            plan.add(a);
        } else {
            int st = dSlice.getStart().getVal();
            rp.insertAllocateAction(plan, vm, src, st, st);
        }
        return true;
    }

    @Override
    public UUID getVM() {
        return vm;
    }

    @Override
    public IntDomainVar getStart() {
        return dSlice.getStart();
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
        return dSlice;
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
