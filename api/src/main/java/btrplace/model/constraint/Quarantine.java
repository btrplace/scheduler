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
import btrplace.plan.event.BootVM;
import btrplace.plan.event.DefaultReconfigurationPlanValidator;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ResumeVM;

import java.util.*;

/**
 * A constraint to put some nodes into quarantine.
 * running VMs in the quarantine zone can not leave their node
 * while no VMs outside the quarantine zone can be hosted on
 * the nodes in quarantine.
 * <p/>
 * The restriction provided by the constraint is only continuous.
 *
 * @author Fabien Hermenier
 */
public class Quarantine extends SatConstraint {

    /**
     * Make a new constraint.
     *
     * @param nodes the set of nodes to put into quarantine
     */
    public Quarantine(Set<UUID> nodes) {
        super(Collections.<UUID>emptySet(), nodes, true);
    }

    @Override
    public Sat isSatisfied(Model i) {
        return Sat.UNDEFINED;
    }

    @Override
    public Sat isSatisfied(ReconfigurationPlan plan) {
        if (plan.getSize() == 0) {
            return Sat.SATISFIED;
        }
        Model src = plan.getOrigin();
        Model mo = plan.getOrigin().clone();
        Map<UUID, Set<UUID>> on = new HashMap<>();
        for (UUID n : getInvolvedNodes()) {
            on.put(n, mo.getMapping().getRunningVMs(n));
        }

        for (Action a : plan) {
            if (!a.apply(mo)) {
                return Sat.UNSATISFIED;
            }
            //New VMs went to the quarantine ?
            for (UUID n : getInvolvedNodes()) {
                Set<UUID> in = new HashSet<>(mo.getMapping().getRunningVMs(n));
                //New VMs went into the node. Violation
                if (!on.get(n).containsAll(in)) {
                    return Sat.UNSATISFIED;
                }
                for (UUID vm : on.get(n)) {
                    if (mo.getMapping().getRunningVMs().contains(vm) &&
                            !mo.getMapping().getVMLocation(vm).equals(src.getMapping().getVMLocation(vm))) {
                        return Sat.UNSATISFIED;
                    }
                }
            }
        }
        return Sat.SATISFIED;
    }

    @Override
    public ReconfigurationPlanValidator getValidator() {
        return new Checker();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Quarantine that = (Quarantine) o;
        return getInvolvedNodes().equals(that.getInvolvedNodes());
    }

    @Override
    public int hashCode() {
        return getInvolvedNodes().hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder("quarantine(")
                .append("nodes=").append(getInvolvedNodes())
                .append(", continuous")
                .append(")").toString();
    }

    @Override
    public boolean setContinuous(boolean b) {
        if (b) {
            return super.setContinuous(b);
        }
        return b;
    }

    /**
     * Checker for the constraint.
     */
    private class Checker extends DefaultReconfigurationPlanValidator {

        @Override
        public boolean accept(BootVM a) {
            return !getInvolvedNodes().contains(a.getDestinationNode());
        }

        @Override
        public boolean accept(MigrateVM a) {
            if (isTracked(a.getVM())) { //the VM can not move elsewhere
                return false;
            }
            return !getInvolvedNodes().contains(a.getDestinationNode());
        }

        @Override
        public boolean accept(ResumeVM a) {
            return !getInvolvedNodes().contains(a.getDestinationNode());
        }

        @Override
        public boolean acceptResultingModel(Model mo) {
            return true;
        }

        @Override
        public boolean acceptOriginModel(Model mo) {
            Mapping map = mo.getMapping();
            return getTrackedVMs().addAll(map.getRunningVMs(getInvolvedNodes()));
        }
    }

    public class Checker2 extends DefaultReconfigurationPlanChecker {

        public Checker2(Set<UUID> vs, Set<UUID> ns) {
            super(vs, ns);
        }

        @Override
        public boolean start(MigrateVM a) {
            if (vms.contains(a.getVM())) { //the VM can not move elsewhere
                return false;
            }
            return startRunningVMPlacement(a);
        }

        @Override
        public boolean startRunningVMPlacement(RunningVMPlacement a) {
            return !nodes.contains(a.getDestinationNode());
        }

        @Override
        public boolean endsWith(Model mo) {
            return true;
        }

        @Override
        public boolean startsWith(Model mo) {
            Mapping map = mo.getMapping();
            vms.clear();
            return vms.addAll(map.getRunningVMs(nodes));
        }

    }
}
