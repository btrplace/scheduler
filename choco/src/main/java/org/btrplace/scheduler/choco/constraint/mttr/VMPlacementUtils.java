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

import org.btrplace.model.Mapping;
import org.btrplace.model.VM;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.chocosolver.solver.variables.IntVar;

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
    public static Map<IntVar, VM> makePlacementMap(ReconfigurationProblem rp) {
        Map<IntVar, VM> m = new HashMap<>();
        for (VM vm : rp.getFutureRunningVMs()) {
            IntVar v = rp.getVMAction(vm).getDSlice().getHoster();
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
    public static boolean canStay(ReconfigurationProblem rp, VM vm) {
        Mapping m = rp.getSourceModel().getMapping();
        if (m.isRunning(vm)) {
            int curPos = rp.getNode(m.getVMLocation(vm));
            return rp.getVMAction(vm).getDSlice().getHoster().contains(curPos);
        }
        return false;
    }
}
