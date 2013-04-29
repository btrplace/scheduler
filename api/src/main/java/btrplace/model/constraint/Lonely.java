/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace.model.constraint;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.plan.*;
import btrplace.plan.event.BootNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A constraint to force all the given VMs, when running,
 * to not share their host with other VMs. Co-location between
 * the VMs given as argument is still possible.
 * <p/>
 * If the restriction is discrete, then the constraint ensures the given VMs
 * will not be co-located with other VMs only at the end of the reconfiguration process.
 * <p/>
 * If the restriction is continuous, then no co-location is possible during the reconfiguration
 * process.
 *
 * @author Fabien Hermenier
 */
public class Lonely extends SatConstraint {

    /**
     * Make a new constraint with a discrete restriction.
     *
     * @param vms the set of VMs to consider
     */
    public Lonely(Set<UUID> vms) {
        this(vms, false);
    }

    /**
     * Make a new constraint.
     *
     * @param vms        the set of VMs to consider
     * @param continuous {@code true} for a continuous restriction
     */
    public Lonely(Set<UUID> vms, boolean continuous) {
        super(vms, Collections.<UUID>emptySet(), continuous);
    }

    @Override
    public Sat isSatisfied(ReconfigurationPlan p) {
        Model mo = p.getOrigin();
        if (!isSatisfied(mo).equals(Sat.SATISFIED)) {
            return Sat.UNSATISFIED;
        }
        mo = p.getOrigin().clone();
        for (Action a : p) {
            if (!a.apply(mo)) {
                return Sat.UNSATISFIED;
            }
            if (!isSatisfied(mo).equals(Sat.SATISFIED)) {
                return Sat.UNSATISFIED;
            }
        }
        return Sat.SATISFIED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Lonely that = (Lonely) o;
        return getInvolvedVMs().equals(that.getInvolvedVMs());
    }

    @Override
    public int hashCode() {
        return getInvolvedVMs().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("lonely(")
                .append("vms=").append(getInvolvedVMs());

        if (!isContinuous()) {
            b.append(", discrete");
        } else {
            b.append(", continuous");
        }

        return b.append(')').toString();
    }

    @Override
    public ReconfigurationPlanChecker getChecker() {
        return new Checker(this);
    }

    /**
     * Checker for the constraint.
     */
    private class Checker extends DefaultReconfigurationPlanChecker {

        private Set<UUID> idleNodes;

        private Set<UUID> privateNodes;

        public Checker(Lonely l) {
            super(l);
            idleNodes = new HashSet<>();
            privateNodes = new HashSet<>();
        }

        private boolean checkDestination(UUID vm, UUID n) {
            if (isContinuous()) {
                if (vms.contains(vm)) {
                    if (!idleNodes.remove(n)) { //The node was not idle
                        return privateNodes.add(n); //So it must be private
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
            for (UUID vm : vms) {
                if (map.getRunningVMs().contains(vm)) {
                    UUID host = map.getVMLocation(vm);
                    Set<UUID> on = map.getRunningVMs(host);
                    //Check for other VMs on the node. If they are not in the constraint
                    //it's a violation
                    for (UUID vm2 : on) {
                        if (!vm2.equals(vm) && !vms.contains(vm2)) {
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
            if (isContinuous()) {
                boolean ret = discreteCheck(mo);
                if (ret) {
                    Mapping map = mo.getMapping();
                    for (UUID vm : vms) {
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
}
