/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint.mttr;

import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.IntVar;

import java.util.List;


/**
 * A variable selector that focuses on the VMs that will be running
 * necessarily on a new node as their current location is disallowed.
 *
 * @author Fabien Hermenier
 */
public class MovingVMs implements VariableSelector<IntVar> {

  /**
   * The demanding slices to consider.
   */
  private final List<VMTransition> actions;

    private final Mapping map;

  private final ReconfigurationProblem rp;

  private final IStateInt idx;

  /**
     * Make a new heuristic.
     * By default, the heuristic doesn't touch the scheduling constraints.
     *
     * @param s       the solver to use to extract the assignment variables
     * @param m       the initial configuration
     * @param actions the actions to consider
     */
    public MovingVMs(ReconfigurationProblem s, Mapping m, List<VMTransition> actions) {
        this.map = m;
        this.actions = actions;
        this.rp = s;
        this.idx = s.getModel().getEnvironment().makeInt(0);
    }

    @SuppressWarnings("squid:S3346")
    private boolean setToNextMovingVM(IntVar[] scopes) {
        assert actions.size() == scopes.length;
        for (int i = idx.get(); i < scopes.length; i++) {
            IntVar h = scopes[i];
            if (!h.isInstantiated()) {
                VM vm = actions.get(i).getVM();
                Node nId = map.getVMLocation(vm);
                if (!h.contains(rp.getNode(nId))) {
                    //VM was running, otherwise -1 so not inside h
                    idx.set(i);
                    return true;
                }
            }
            i++;
        }
        return false;
    }

    @Override
    public IntVar getVariable(IntVar[] scopes) {
        return setToNextMovingVM(scopes) ? scopes[idx.get()] : null;
    }
}
