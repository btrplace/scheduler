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

package btrplace.solver.choco.objective.minMTTR;

import btrplace.model.Mapping;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.actionModel.VMActionModel;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * A variable selector that focuses on the VMs that will be running
 * necessarily on a new node as their current location is disallowed.
 *
 * @author Fabien Hermenier
 */
public class MovingVMs extends AbstractIntVarSelector {

    /**
     * The demanding slices to consider.
     */
    private List<VMActionModel> actions;

    private Mapping map;

    private ReconfigurationProblem rp;

    private String label;

    /**
     * Make a new heuristic.
     * By default, the heuristic doesn't touch the scheduling constraints.
     *
     * @param s   the solver to use to extract the assignment variables
     * @param m   the initial configuration
     * @param vms the VMs to consider
     */
    public MovingVMs(String label, ReconfigurationProblem s, Mapping m, Set<Integer> vms) {
        super(s.getSolver());
        this.label = label;
        map = m;

        this.rp = s;
        this.actions = new LinkedList<>();
        //Get all the involved slices
        for (int vm : vms) {
            if (rp.getFutureRunningVMs().contains(vm)) {
                actions.add(rp.getVMAction(vm));
            }
        }
    }

    @Override
    public IntDomainVar selectVar() {
        for (VMActionModel a : actions) {
            if (!a.getDSlice().getHoster().isInstantiated()) {
                int vm = a.getVM();
                int nId = map.getVMLocation(vm);
                if (nId >= 0) {
                    //VM was running
                    Slice slice = a.getDSlice();
                    if (!slice.getHoster().canBeInstantiatedTo(rp.getNodeIdx(nId))) {
                        return slice.getHoster();
                    }
                }
            }
        }
        rp.getLogger().debug("{} - No more VMs to handle", label);
        return null;
    }
}
