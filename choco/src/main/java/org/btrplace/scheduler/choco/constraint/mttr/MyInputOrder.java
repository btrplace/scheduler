/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint.mttr;

import org.btrplace.scheduler.choco.constraint.CObjective;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.Variable;

/**
 * A clone of {@link org.chocosolver.solver.search.strategy.selectors.variables.InputOrder}
 * It only calls {@link CObjective#postCostConstraints()} before choosing a variable if not null
 *
 * @author Fabien Hermenier
 */
public class MyInputOrder<V extends Variable> implements VariableSelector<V> {

  private final CObjective obj;

  private final IStateInt last;

    /**
     * New heuristic.
     *
     * @param s the solver in use
     * @param o an optional objective to consider
     */
    public MyInputOrder(Solver s, CObjective o) {
        obj = o;
        last = s.getEnvironment().makeInt(0);
    }

    /**
     * New heuristic.
     *
     * @param s the solver in use
     */
    public MyInputOrder(Solver s) {
        this(s, null);
    }

    @Override
    public V getVariable(V[] variables) {
        if (obj != null) {
            obj.postCostConstraints();
        }
        for (int idx = last.get(); idx < variables.length; idx++) {
            if (!variables[idx].isInstantiated()) {
                last.set(idx);
                return variables[idx];
            }
        }
        last.set(variables.length);
        return null;
    }
}
