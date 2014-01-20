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
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import solver.Solver;
import solver.variables.IntVar;
import solver.variables.VariableFactory;


/**
 * A model for a running VM that stay online on the same node.
 *
 * @author Fabien Hermenier
 */
public class StayRunningVMModel implements KeepRunningVMModel {

    private Slice cSlice, dSlice;

    private ReconfigurationProblem rp;

    private VM vm;

    private IntVar stay;

    /**
     * Make a new model.
     *
     * @param p the RP to use as a basis.
     * @param e the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public StayRunningVMModel(ReconfigurationProblem p, VM e) throws SolverException {
        this.vm = e;
        this.rp = p;
        IntVar host = p.makeCurrentHost("stayRunningVM(" + e + ").host", e);
        cSlice = new SliceBuilder(p, e, "stayRunningVM(" + e + ").cSlice")
                .setHoster(host)
                .setEnd(p.makeUnboundedDuration("stayRunningVM(", e, ").cSlice_end"))
                .build();
        dSlice = new SliceBuilder(p, e, "stayRunningVM(" + e + ").dSlice")
                .setHoster(host)
                .setStart(cSlice.getEnd())
                .build();
        Solver s = p.getSolver();

        stay = VariableFactory.one(s);
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        return true;
    }

    @Override
    public IntVar getStart() {
        return rp.getStart();
    }

    @Override
    public IntVar getEnd() {
        return rp.getStart();
    }

    @Override
    public IntVar getDuration() {
        return rp.getStart();
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
    public IntVar getState() {
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
    public IntVar isStaying() {
        return stay;
    }
}
