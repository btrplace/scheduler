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

package btrplace.solver.choco.objective.minMTTR;

import btrplace.solver.SolverException;
import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.ReconfigurationObjective;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.Choco;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.List;

/**
 * An objective that minimize the time to repair a non-viable model.
 *
 * @author Fabien Hermenier
 */
public class MinMTTR implements ReconfigurationObjective {

    public MinMTTR() {

    }

    @Override
    public void inject(ReconfigurationProblem rp) throws SolverException {
        List<IntDomainVar> mttrs = new ArrayList<IntDomainVar>();
        for (ActionModel m : rp.getVMActions()) {
            mttrs.add(m.getEnd());
        }
        for (ActionModel m : rp.getNodeActions()) {
            mttrs.add(m.getEnd());
        }
        IntDomainVar[] costs = mttrs.toArray(new IntDomainVar[mttrs.size()]);
        CPSolver s = rp.getSolver();
        IntDomainVar cost = s.createBoundIntVar(rp.makeVarLabel("globalCost"), 0, Choco.MAX_UPPER_BOUND);
        s.post(s.eq(cost, s.sum(costs)));

        s.setDoMaximize(false);
        s.setObjective(cost);
    }
}
