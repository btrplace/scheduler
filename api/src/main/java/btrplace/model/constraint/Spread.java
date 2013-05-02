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

import btrplace.model.SatConstraint;
import btrplace.model.constraint.checker.SatConstraintChecker;
import btrplace.model.constraint.checker.SpreadChecker;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * A constraint to indicate that the given VMs, if running,
 * must be hosted on distinct nodes.
 * <p/>
 * If the restriction is continuous, the constraint ensure no VMs are relocated to a node hosting a VM
 * involved in the same Spread constraint.
 * <p/>
 * If the restriction is discrete, the constraint only ensures that there is no co-location
 * at the end of the reconfiguration plan.
 *
 * @author Fabien Hermenier
 */
public class Spread extends SatConstraint {

    /**
     * Make a new constraint having a continuous restriction.
     *
     * @param vms the VMs to consider
     */
    public Spread(Set<UUID> vms) {
        this(vms, true);
    }

    /**
     * Make a new constraint.
     *
     * @param vms        the VMs to consider
     * @param continuous {@code true} for a continuous restriction.
     */
    public Spread(Set<UUID> vms, boolean continuous) {
        super(vms, Collections.<UUID>emptySet(), continuous);
    }

    /*@Override
    public Sat isSatisfied(ReconfigurationPlan plan) {
        if (plan.getSize() == 0) {
            return isSatisfied(plan.getOrigin());
        }

        //For each relocation action, we check if the
        //destination node is not hosting a VM involved in the constraint
        Model cur = plan.getOrigin().clone();
        for (Action a : plan) {
            if (!a.apply(cur)) {
                return Sat.UNSATISFIED;
            }

            UUID destNode;
            if (a instanceof RunningVMPlacement) {
                destNode = ((RunningVMPlacement) a).getDestinationNode();
                Set<UUID> on = new HashSet<>(cur.getMapping().getRunningVMs(destNode));
                //If there is 2 VMs here that are involved in
                //the constraint, it's a failure
                on.retainAll(getInvolvedVMs());
                if (on.size() > 1) {
                    return Sat.UNSATISFIED;
                }
            }
        }
        return Sat.SATISFIED;
    }   */

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("spread(vms=").append(getInvolvedVMs());
        if (!isContinuous()) {
            b.append(", discrete");
        } else {
            b.append(", continuous");
        }
        return b.append(')').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Spread that = (Spread) o;
        return getInvolvedVMs().equals(that.getInvolvedVMs()) && isContinuous() == that.isContinuous();
    }

    @Override
    public int hashCode() {
        return getInvolvedVMs().hashCode();
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new SpreadChecker(this);
    }


}
