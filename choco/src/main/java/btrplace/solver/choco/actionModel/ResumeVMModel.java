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

import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.ResumeVM;
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
 * Model an action that resume a sleeping VM.
 * The model must provide an estimation of the action duration through a
 * {@link btrplace.solver.choco.durationEvaluator.ActionDurationEvaluator} accessible from
 * {@link btrplace.solver.choco.ReconfigurationProblem#getDurationEvaluators()} with the key {@code ResumeVM.class}
 * <p/>
 * If the reconfiguration problem has a solution, a {@link btrplace.plan.event.ResumeVM} action
 * is inserted into the resulting reconfiguration plan.
 *
 * @author Fabien Hermenier
 */
public class ResumeVMModel implements VMActionModel {

    private VM vm;

    private ReconfigurationProblem rp;

    private IntVar start;

    private IntVar end;

    private IntVar duration;

    private Slice dSlice;

    private BoolVar state;

    /**
     * Make a new model.
     *
     * @param p the RP to use as a basis.
     * @param e the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public ResumeVMModel(ReconfigurationProblem p, VM e) throws SolverException {
        this.rp = p;
        this.vm = e;

        int d = p.getDurationEvaluators().evaluate(p.getSourceModel(), ResumeVM.class, e);

        start = p.makeDuration(p.getEnd().getUB() - d, 0, "resumeVM(", e, ").start");
        end = VariableFactory.offset(start, d);
        duration = p.makeDuration(d, d, "resumeVM(", e, ").duration");
        dSlice = new SliceBuilder(p, e, "resumeVM(" + e + ").dSlice").setStart(start)
                .setDuration(p.makeDuration(p.getEnd().getUB(), d, "resumeVM(", e, ").dSlice_duration"))
                .build();

        Solver s = p.getSolver();
        s.post(IntConstraintFactory.arithm(end, "<=", p.getEnd()));
        state = VariableFactory.one(rp.getSolver());
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        int ed = end.getValue();
        int st = start.getValue();
        Node src = rp.getSourceModel().getMapping().getVMLocation(vm);
        Node dst = rp.getNode(dSlice.getHoster().getValue());
        ResumeVM a = new ResumeVM(vm, src, dst, st, ed);
        plan.add(a);
        return true;
    }

    @Override
    public VM getVM() {
        return vm;
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
        return duration;
    }

    @Override
    public Slice getCSlice() {
        return null;
    }

    @Override
    public Slice getDSlice() {
        return dSlice;
    }

    @Override
    public BoolVar getState() {
        return state;
    }

    @Override
    public void visit(ActionModelVisitor v) {
        v.visit(this);
    }

}
