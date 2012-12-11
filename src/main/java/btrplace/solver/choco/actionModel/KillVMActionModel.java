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

import btrplace.model.Mapping;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.KillVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import btrplace.solver.choco.VMActionModel;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * An action to model a VM that is killed.
 * The kill necessarily occurs at the beginning of the reconfiguration process and
 * can consider a VM that is either in the ready, the running, and the sleeping state.
 *
 * @author Fabien Hermenier
 */
public class KillVMActionModel implements VMActionModel {

    private UUID vm;

    private UUID node;

    private IntDomainVar state;

    private IntDomainVar start;

    private IntDomainVar end;

    private Slice cSlice;

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public KillVMActionModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        vm = e;
        Mapping map = rp.getSourceModel().getMapping();
        node = map.getVMLocation(vm);
        state = rp.getSolver().makeConstantIntVar(0);

        int d = rp.getDurationEvaluators().evaluate(KillVM.class, e);

        if (map.getRunningVMs().contains(vm)) {
            cSlice = new SliceBuilder(rp, e, "killVM('" + e + "').cSlice")
                    .setExclusive(false)
                    .setStart(rp.getStart())
                    .setHoster(rp.getCurrentVMLocation(rp.getVM(vm)))
                    .setEnd(rp.getSolver().makeConstantIntVar(d))
                    .build();
            end = cSlice.getEnd();
        } else {
            end = rp.getSolver().makeConstantIntVar(d);
        }
        start = rp.getStart();


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
        return end;
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
    public UUID getVM() {
        return vm;
    }

    @Override
    public void visit(ActionModelVisitor v) {
        v.visit(this);
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        plan.add(new KillVM(vm, node, getStart().getVal(), getEnd().getVal()));
        return true;
    }

    @Override
    public IntDomainVar getState() {
        return state;
    }
}
