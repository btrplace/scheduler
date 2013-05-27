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
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.HashMap;
import java.util.Map;


/**
 * Tools to ease the management of the VM placement.
 *
 * @author Fabien Hermenier
 */
public final class VMPlacementUtils {

    private VMPlacementUtils() {
    }

    /**
     * Map a map where keys are the placement variable of the future-running VMs
     * and values are the VM identifier.
     *
     * @param rp the problem
     * @return the resulting map.
     */
    public static Map<IntDomainVar, Integer> makePlacementMap(ReconfigurationProblem rp) {
        Map<IntDomainVar, Integer> m = new HashMap<>();
        for (int vm : rp.getFutureRunningVMs()) {
            IntDomainVar v = rp.getVMAction(vm).getDSlice().getHoster();
            m.put(v, vm);
        }
        return m;
    }

    /**
     * Check if a VM can stay on its current node.
     *
     * @param rp the reconfiguration problem.
     * @param vm the VM
     * @return {@code true} iff the VM can stay
     */
    public static boolean canStay(ReconfigurationProblem rp, int vm) {
        Mapping m = rp.getSourceModel().getMapping();
        if (m.getRunningVMs().contains(vm)) {
            int curPos = rp.getNodeIdx(m.getVMLocation(vm));
            return rp.getVMAction(vm).getDSlice().getHoster().canBeInstantiatedTo(curPos);
        }
        return false;
    }
}
