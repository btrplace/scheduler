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

package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Lonely;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.RunningVMPlacement;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Lonely} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Lonely
 */
public class LonelyChecker extends AllowAllConstraintChecker<Lonely> {

    private Set<UUID> idleNodes;

    private Set<UUID> privateNodes;

    /**
     * Make a new checker.
     *
     * @param l the associated constraint
     */
    public LonelyChecker(Lonely l) {
        super(l);
        idleNodes = new HashSet<>();
        privateNodes = new HashSet<>();
    }

    private boolean checkDestination(UUID vm, UUID n) {
        if (getConstraint().isContinuous()) {
            if (getVMs().contains(vm)) {
                if (!idleNodes.remove(n)) {
                    //The node was not idle
                    //So it must be private
                    return privateNodes.add(n);
                }
                //The node is now longer idle, just private
                return privateNodes.add(n);
            } else {
                //Not tracked, so just don't go on a private node
                if (!idleNodes.remove(n)) {
                    return !privateNodes.contains(n);
                }
            }
        }
        return true;
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        return checkDestination(a.getVM(), a.getDestinationNode());
    }

    private boolean discreteCheck(Model mo) {
        Mapping map = mo.getMapping();
        for (UUID vm : getVMs()) {
            if (map.getRunningVMs().contains(vm)) {
                UUID host = map.getVMLocation(vm);
                Set<UUID> on = map.getRunningVMs(host);
                //Check for other VMs on the node. If they are not in the constraint
                //it's a violation
                for (UUID vm2 : on) {
                    if (!vm2.equals(vm) && !getVMs().contains(vm2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean start(BootNode a) {
        return idleNodes.add(a.getNode());
    }

    @Override
    public boolean endsWith(Model mo) {
        return discreteCheck(mo);
    }

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            boolean ret = discreteCheck(mo);
            if (ret) {
                Mapping map = mo.getMapping();
                for (UUID vm : getVMs()) {
                    if (map.getRunningVMs().contains(vm)) {
                        privateNodes.add(map.getVMLocation(vm));
                    }
                }
                for (UUID n : map.getOnlineNodes()) {
                    if (map.getRunningVMs(n).isEmpty()) {
                        idleNodes.add(n);
                    }
                }
            }
            return ret;
        }
        return true;
    }
}
