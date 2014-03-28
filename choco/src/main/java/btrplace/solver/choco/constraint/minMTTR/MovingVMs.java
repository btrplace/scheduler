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

package btrplace.solver.choco.constraint.minMTTR;

import btrplace.model.Mapping;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.SliceUtils;
import btrplace.solver.choco.actionModel.ActionModelUtils;
import btrplace.solver.choco.actionModel.VMActionModel;
import memory.IStateInt;
import solver.search.strategy.selectors.VariableSelector;
import solver.variables.IntVar;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;


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
    private List<VMActionModel> actions;

    private Mapping map;

    private ReconfigurationProblem rp;

    private IntVar[] scopes;

    private IStateInt idx;

    /**
     * Make a new heuristic.
     * By default, the heuristic doesn't touch the scheduling constraints.
     *
     * @param s   the solver to use to extract the assignment variables
     * @param m   the initial configuration
     * @param vms the VMs to consider
     */
    public MovingVMs(ReconfigurationProblem s, Mapping m, Set<VM> vms) {
        map = m;

        this.rp = s;
        this.actions = new LinkedList<>();
        //Get all the involved slices
        for (VM vm : vms) {
            if (rp.getFutureRunningVMs().contains(vm)) {
                actions.add(rp.getVMAction(vm));
            }
        }
        scopes = SliceUtils.extractHoster(ActionModelUtils.getDSlices(actions));
        idx = s.getSolver().getEnvironment().makeInt(0);
    }

    @Override
    public IntVar[] getScope() {
        return scopes;
    }

    private boolean setToNextMovingVM() {
        for (int i = idx.get(); i < scopes.length; i++) {
            IntVar h = scopes[i];
            if (!h.instantiated()) {
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
    public boolean hasNext() {
        return setToNextMovingVM();
    }

    @Override
    public void advance() {
        setToNextMovingVM();
    }

    @Override
    public IntVar getVariable() {
        return scopes[idx.get()];
    }
}
