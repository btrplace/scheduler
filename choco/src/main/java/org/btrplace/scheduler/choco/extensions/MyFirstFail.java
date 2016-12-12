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
 * Tuned version of choco FirstFail.
 * Remember the index of the last instantiated variable to avoid to start iterating at 0.
 * Stop with the first domain of size 2.
 * @author Fabien Hermenier
 */
public class MyFirstFail implements VariableSelector<IntVar>, VariableEvaluator<IntVar> {

    private IStateInt last;

    private String label;
    /**
     * New heuristic.
     *
     * @param s the solver in use
     */
    public MyFirstFail(Solver s) {
        last = s.getEnvironment().makeInt(0);
    }

    public MyFirstFail(Solver s, String lbl) {

        last = s.getEnvironment().makeInt(0);
        this.label = lbl;
    }

    @Override
    public IntVar getVariable(IntVar[] variables) {
        int idx = -1;
        int dsize = Integer.MAX_VALUE;
        boolean got = false;
        for (int i = last.get(); i < variables.length; i++) {
            if (!got && !variables[i].isInstantiated()) {
                last.set(i);
                got = true;
            }
            int d = variables[i].getDomainSize();
            if (d > 1 && d < dsize) {
                dsize = d;
                idx = i;
            }
            if (d == 2) {
                break;
            }
        }
        return idx > -1 ? variables[idx] : null;
    }

    @Override
    public double evaluate(IntVar variable) {
        return variable.getDomainSize();
    }
}
