/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.KillVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;


/**
 * An action to model a VM that is killed.
 * The model must provide an estimation of the action duration through a
 * {@link btrplace.solver.choco.durationEvaluator.ActionDurationEvaluator} accessible from
 * {@link btrplace.solver.choco.ReconfigurationProblem#getDurationEvaluators()} with the key {@code KillVM.class}
 * <p/>
 * If the reconfiguration problem has a solution, a {@link btrplace.plan.event.KillVM} action
 * is inserted into the resulting reconfiguration plan.
 * <p/>
 * The kill necessarily occurs at the beginning of the reconfiguration process and
 * can consider a VM that is either in the ready, the running, and the sleeping state.
 *
 * @author Fabien Hermenier
 */
public class KillVMActionModel implements VMActionModel {

    private VM vm;

    private Node node;

    private BoolVar state;

    private IntVar start;

    private IntVar end;

    private Slice cSlice;

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public KillVMActionModel(ReconfigurationProblem rp, VM e) throws SolverException {
        vm = e;
        Mapping map = rp.getSourceModel().getMapping();
        node = map.getVMLocation(vm);
        state = VariableFactory.zero(rp.getSolver());

        int d = rp.getDurationEvaluators().evaluate(rp.getSourceModel(), KillVM.class, e);

        if (map.isRunning(vm)) {
            cSlice = new SliceBuilder(rp, e, "killVM('" + e + "').cSlice")
                    .setStart(rp.getStart())
                    .setHoster(rp.getCurrentVMLocation(rp.getVM(vm)))
                    .setEnd(VariableFactory.fixed(d, rp.getSolver()))
                    .build();
            end = cSlice.getEnd();
        } else {
            end = VariableFactory.fixed(d, rp.getSolver());
        }
        start = rp.getStart();


    }

    @Override
    public IntVar getStart() {
        return start;
    }

    @Override
    public IntVar getEnd() {
        return end;
    }

    @Override
    public IntVar getDuration() {
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
    public VM getVM() {
        return vm;
    }

    @Override
    public void visit(ActionModelVisitor v) {
        v.visit(this);
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        plan.add(new KillVM(vm, node, getStart().getValue(), getEnd().getValue()));
        return true;
    }

    @Override
    public BoolVar getState() {
        return state;
    }
}
