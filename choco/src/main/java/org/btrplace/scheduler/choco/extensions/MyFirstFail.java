/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.scheduler.choco.extensions;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.selectors.VariableEvaluator;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.variables.IntVar;

/**
 * @author Fabien Hermenier
 */
public class MyFirstFail implements VariableSelector<IntVar>, VariableEvaluator<IntVar> {

    private IStateInt last;

    /**
     * New heuristic.
     *
     * @param s the solver in use
     */
    public MyFirstFail(Solver s) {

        last = s.getEnvironment().makeInt(0);
    }

    @Override
    public IntVar getVariable(IntVar[] variables) {
        int small_idx = -1;
        int small_dsize = Integer.MAX_VALUE;
        boolean got = false;
        for (int idx = last.get(); idx < variables.length; idx++) {
            if (!got && !variables[idx].isInstantiated()) {
                last.set(idx);
                got = true;
            }
            int dsize = variables[idx].getDomainSize();
            if (dsize > 1 && dsize < small_dsize) {
                small_dsize = dsize;
                small_idx = idx;
            }
            if (small_dsize == 2) {
                break;
            }
        }
        return small_idx > -1 ? variables[small_idx] : null;
    }

    @Override
    public double evaluate(IntVar variable) {
        return variable.getDomainSize();
    }
}
