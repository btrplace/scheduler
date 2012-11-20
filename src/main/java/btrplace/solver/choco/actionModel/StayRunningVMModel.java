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
import btrplace.plan.SolverException;
import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A model for a running VM that stay online.
 */
public class StayRunningVMModel extends ActionModel {

    public StayRunningVMModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        super(rp, e);

        CPSolver s = rp.getSolver();
        boolean neadIncrease = true; //TODO: How to get resource changes ?
        IntDomainVar host = rp.makeCurrentHost("", e);
        if (neadIncrease) {
            cSlice = new Slice("", e, rp.getStart(), rp.getEnd(), rp.getEnd(), host, s.createIntegerConstant("", 0));
            dSlice = new Slice("", e, rp.getEnd(), rp.getEnd(), rp.makeDuration("", 0, 0), host, s.createIntegerConstant("", 0));
        } else {
            cSlice = new Slice("", e, rp.getStart(), rp.getStart(), rp.getStart(), host, s.createIntegerConstant("", 0));
            dSlice = new Slice("", e, rp.getStart(), rp.getEnd(), rp.getEnd(), host, s.createIntegerConstant("", 0));
        }
        end = dSlice.getStart();
        start = cSlice.getEnd();
        cost = s.createIntegerConstant("", 0);
        duration = cost;
        s.post(s.eq(this.end, this.start));
    }

    @Override
    public List<Action> getResultingActions(ReconfigurationProblem rp) {
        return new ArrayList<Action>();
    }
}
