package org.btrplace.scheduler.choco.constraint.mttr;

import org.btrplace.scheduler.choco.constraint.CObjective;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.variables.Variable;

/**
 * A clone of {@link org.chocosolver.solver.search.strategy.selectors.variables.InputOrder}
 * It only calls {@link CObjective#postCostConstraints()} before choosing a variable.
 *
 * @author Fabien Hermenier
 */
public class MyInputOrder<V extends Variable> implements VariableSelector<V> {

    private CObjective obj;

    public MyInputOrder(CObjective o) {
        obj = o;
    }

    @Override
    public V getVariable(V[] variables) {
        obj.postCostConstraints();
        int small_idx = -1;
        for (int idx = 0; idx < variables.length; idx++) {
            if (!variables[idx].isInstantiated()) {
                small_idx = idx;
                break;
            }
        }
        return small_idx > -1 ? variables[small_idx] : null;
    }
}
