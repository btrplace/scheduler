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
import btrplace.plan.event.ForgeVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import btrplace.solver.choco.VMActionModel;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * Model an action that forge a VM to put it into the ready state. *
 * <p/>
 * The VM must have an attribute (provided by {@link btrplace.model.Model#getAttributes()}
 * {@code template} that indicate the template identifier to use to build the VM image.
 * <p/>
 * The model must provide an estimation of the action duration through a
 * {@link btrplace.solver.choco.DurationEvaluator} accessible from
 * {@link btrplace.solver.choco.ReconfigurationProblem#getDurationEvaluators()} with the key {@code ForgeVM.class}
 * <p/>
 * If the reconfiguration problem has a solution, a {@link btrplace.plan.event.ForgeVM} action
 * will inserted into the resulting reconfiguration plan.
 *
 * @author Fabien Hermenier
 */
public class ForgeVMModel implements VMActionModel {

    private UUID vm;

    private IntDomainVar duration;

    private IntDomainVar state;

    private Slice dSlice;

    private String template;

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public ForgeVMModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        int d = rp.getDurationEvaluators().evaluate(ForgeVM.class, e);
        template = rp.getSourceModel().getAttributes().getString(e, "template");
        if (template == null) {
            throw new SolverException(rp.getSourceModel(), "Unable to forge the VM '" + e + "'. The required attribute 'template' is missing from the model");
        }
        CPSolver s = rp.getSolver();
        duration = s.makeConstantIntVar(d);
        state = s.makeConstantIntVar(0);
        vm = e;

        /*
         * We don't make any "real" d-slice cause it may impacts the TaskScheduler
         * so the hosting variable is set to -1 to be sure the VM is not hosted on a node
         */
        dSlice = new SliceBuilder(rp, e, rp.makeVarLabel("forge(" + e + ").dSlice"))
                .setDuration(duration)
                .setStart(rp.makeDuration("forge(" + e + ").start"))
                .setEnd(rp.makeDuration("forge(" + e + ").stop"))
                .setHoster(-1)
                .build();
        s.post(s.leq(d, dSlice.getDuration()));
        s.post(s.leq(dSlice.getEnd(), rp.getEnd()));
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        ForgeVM a = new ForgeVM(vm, getStart().getVal(), getEnd().getVal());
        return plan.add(a);
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
        return dSlice.getEnd();
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

    /**
     * Get the template to use to build the VM.
     *
     * @return the template identifier
     */
    public String getTemplate() {
        return template;
    }
}
