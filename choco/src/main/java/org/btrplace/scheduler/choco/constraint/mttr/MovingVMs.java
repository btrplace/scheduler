/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco.constraint.mttr;

import org.chocosolver.memory.IStateInt;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
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
    private List<VMTransition> actions;

    private Mapping map;

    private ReconfigurationProblem rp;

    private IStateInt idx;

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
        this.idx = s.getSolver().getEnvironment().makeInt(0);
    }

    private boolean setToNextMovingVM(IntVar[] scopes) {
        assert actions.size() == scopes.length;
        for (int i = idx.get(); i < scopes.length; i++) {
            IntVar h = scopes[i];
            if (!h.isInstantiated()) {
                VM vm = actions.get(i).getVM();
                Node nId = map.getVMLocation(vm);
                if (nId != null) {
                    //VM was running
                    if (!h.contains(rp.getNode(nId))) {
                        idx.set(i);
                        return true;
                    }
                }
            }
            i++;
        }
        return false;
    }

    @Override
    public IntVar getVariable(IntVar[] scopes) {
        return (setToNextMovingVM(scopes)) ? scopes[idx.get()] : null;
    }
}
