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

import btrplace.model.VM;
import btrplace.model.VMState;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.ForgeVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;


/**
 * Model an action that forge a VM to put it into the ready state. *
 * <p/>
 * The VM must have an attribute (provided by {@link btrplace.model.Model#getAttributes()}
 * {@code template} that indicate the template identifier to use to build the VM image.
 * <p/>
 * The model must provide an estimation of the action duration through a
 * {@link btrplace.solver.choco.durationEvaluator.ActionDurationEvaluator} accessible from
 * {@link btrplace.solver.choco.ReconfigurationProblem#getDurationEvaluators()} with the key {@code ForgeVM.class}
 * <p/>
 * If the reconfiguration problem has a solution, a {@link btrplace.plan.event.ForgeVM} action
 * will inserted into the resulting reconfiguration plan.
 *
 * @author Fabien Hermenier
 */
public class ForgeVMModel implements VMActionModel {

    private VM vm;

    private IntVar duration;

    private BoolVar state;

    private Slice dSlice;

    private String template;

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public ForgeVMModel(ReconfigurationProblem rp, VM e) throws SolverException {
        int d = rp.getDurationEvaluators().evaluate(rp.getSourceModel(), ForgeVM.class, e);
        template = rp.getSourceModel().getAttributes().getString(e, "template");
        if (template == null) {
            throw new SolverException(rp.getSourceModel(), "Unable to forge the VM '" + e + "'. The required attribute 'template' is missing from the model");
        }
        Solver s = rp.getSolver();
        duration = VariableFactory.fixed(d, s);
        state = VariableFactory.zero(s);
        vm = e;

        /*
         * We don't make any "real" d-slice cause it may impacts the TaskScheduler
         * so the hosting variable is set to -1 to be sure the VM is not hosted on a node
         */
        dSlice = new SliceBuilder(rp, e, rp.makeVarLabel("forge(", e, ").dSlice"))
                .setDuration(duration)
                .setStart(rp.makeUnboundedDuration("forge(", e, ").start"))
                .setEnd(rp.makeUnboundedDuration("forge(", e, ").stop"))
                .setHoster(-1)
                .build();
        s.post(IntConstraintFactory.arithm(dSlice.getDuration(), ">=", d));
        s.post(IntConstraintFactory.arithm(dSlice.getEnd(), "<=", rp.getEnd()));
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        ForgeVM a = new ForgeVM(vm, getStart().getValue(), getEnd().getValue());
        return plan.add(a);
    }

    @Override
    public VM getVM() {
        return vm;
    }

    @Override
    public IntVar getStart() {
        return dSlice.getStart();
    }

    @Override
    public IntVar getEnd() {
        return dSlice.getEnd();
    }

    @Override
    public IntVar getDuration() {
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
    public BoolVar getState() {
        return state;
    }

    @Override
    public boolean isManaged() {
        return true;
    }


    /**
     * Get the template to use to build the VM.
     *
     * @return the template identifier
     */
    public String getTemplate() {
        return template;
    }

    public static class Builder extends VMActionModelBuilder {

        public Builder() {
            super("forge", VMState.INIT, VMState.READY);
        }

        @Override
        public VMActionModel build(ReconfigurationProblem r, VM v) throws SolverException {
            return new ForgeVMModel(r, v);
        }
    }
}
