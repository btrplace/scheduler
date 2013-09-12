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
import choco.cp.solver.CPSolver;
import choco.cp.solver.variables.integer.IntDomainVarAddCste;
import choco.kernel.solver.variables.integer.IntDomainVar;


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

    private IntDomainVar start;

    private IntDomainVar end;

    private IntDomainVar duration;

    private Slice dSlice;

    private IntDomainVar state;

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

        start = p.makeDuration(p.getEnd().getSup() - d, 0, "resumeVM(", e, ").start");
        end = new IntDomainVarAddCste(p.getSolver(), p.makeVarLabel("resumeVM(", e, ").end"), start, d);
        duration = p.makeDuration(d, d, "resumeVM(", e, ").duration");
        dSlice = new SliceBuilder(p, e, "resumeVM(" + e + ").dSlice").setStart(start)
                .setDuration(p.makeDuration(p.getEnd().getSup(), d, "resumeVM(", e, ").dSlice_duration"))
                .build();

        CPSolver s = p.getSolver();
        s.post(s.leq(end, p.getEnd()));
        state = s.makeConstantIntVar(1);
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        int ed = end.getVal();
        int st = start.getVal();
        Node src = rp.getSourceModel().getMapping().getVMLocation(vm);
        Node dst = rp.getNode(dSlice.getHoster().getVal());
        ResumeVM a = new ResumeVM(vm, src, dst, st, ed);
        plan.add(a);
        return true;
    }

    @Override
    public VM getVM() {
        return vm;
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
    public IntDomainVar getState() {
        return state;
    }

    @Override
    public void visit(ActionModelVisitor v) {
        v.visit(this);
    }

}
