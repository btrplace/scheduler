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
import btrplace.plan.action.MigrateVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import btrplace.solver.choco.chocoUtil.FastIFFEq;
import btrplace.solver.choco.chocoUtil.FastImpliesEq;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.TimesXYZ;
import choco.cp.solver.constraints.reified.ReifiedFactory;
import choco.cp.solver.variables.integer.BoolVarNot;
import choco.cp.solver.variables.integer.BooleanVarImpl;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model an action that allow a VM to be migrated if necessary.
 * TODO: Integrate re-instantiable experiments
 *
 * @author Fabien Hermenier
 */
public class RelocatableVMModel implements ActionModel {

    private Slice cSlice, dSlice;

    private ReconfigurationProblem rp;

    private UUID vm;

    private IntDomainVar cost;

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
        cost = rp.makeDuration(rp.makeVarLabel("relocatable_cost(" + e + ")"));
        duration = s.createEnumIntVar(rp.makeVarLabel("relocatable_duration(" + e + ")"), new int[]{0, d});
        cSlice = new SliceBuilder(rp, e)
                .setHoster(rp.getNode(rp.getSourceModel().getMapping().getVMLocation(e)))
                .setEnd(rp.makeDuration(rp.makeVarLabel("cSlice_duration(" + e + ")")))
                .setExclusive(false)
                .build();

        dSlice = new SliceBuilder(rp, e)
                .setStart(rp.makeDuration(rp.makeVarLabel("dSlice_duration(" + e + ")")))
                .setExclusive(false)
                .build();
        IntDomainVar move = s.createBooleanVar(rp.makeVarLabel("relocatable_move(" + e + ")"));
        s.post(ReifiedFactory.builder(move, s.neq(cSlice.getHoster(), dSlice.getHoster()), s));

        IntDomainVar stay = new BoolVarNot(s, rp.makeVarLabel("relocatable_stay(" + e + ")"), (BooleanVarImpl) move);

        s.post(new TimesXYZ(move, cSlice.getEnd(), cost));

        s.post(new FastIFFEq(stay, duration, 0));

        boolean increase = false; //TODO: detect increasing requirements
        if (!increase) {
            s.post(new FastImpliesEq(stay, cSlice.getDuration(), 0));
        } else {
            s.post(new FastImpliesEq(stay, dSlice.getDuration(), 0));
        }
        s.post(s.leq(duration, cSlice.getDuration()));
        s.post(s.leq(duration, dSlice.getDuration()));
        s.post(s.eq(cSlice.getEnd(), s.plus(dSlice.getStart(), duration)));

        //TODO: What about the exlusive dSlice stuff ?
        s.post(s.leq(cSlice.getDuration(), rp.getEnd()));
        s.post(s.leq(dSlice.getDuration(), rp.getEnd()));
    }

    @Override
    public List<Action> getResultingActions() {
        List<Action> l = new ArrayList<Action>();
        if (cSlice.getHoster().getVal() != dSlice.getHoster().getVal()) {
            l.add(new MigrateVM(vm,
                    rp.getNode(cSlice.getHoster().getVal()),
                    rp.getNode(dSlice.getHoster().getVal()),
                    getStart().getVal(),
                    getEnd().getVal()));
        }
        return l;
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
        return null;
    }

    @Override
    public void visit(ActionModelVisitor v) {
        v.visit(this);
    }

}
