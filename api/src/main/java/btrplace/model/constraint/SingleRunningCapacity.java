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
import btrplace.plan.event.*;

import java.util.*;

/**
 * Restrict the hosting capacity of each of the given server to a given
 * amount of VMs.
 * <p/>
 * The restriction provided by the constraint can be either discrete or continuous.
 * If it is discrete, the constraint only considers the model obtained as the end
 * of the reconfiguration process.
 * <p/>
 * If the restriction is continuous, then the usage must never exceed
 * the given amount, in the source model, during the reconfiguration and at the end.
 *
 * @author Fabien Hermenier
 */
public class SingleRunningCapacity extends SatConstraint {

    private int amount;

    /**
     * Make a new constraint having a discrete restriction.
     *
     * @param nodes the involved servers.
     * @param qty   the maximum amount of resource to share among the hosted VMs
     */
    public SingleRunningCapacity(Set<UUID> nodes, int qty) {
        this(nodes, qty, false);
    }

    /**
     * Make a new constraint.
     *
     * @param nodes      the involved servers.
     * @param qty        the maximum amount of resource to share among the hosted VMs
     * @param continuous {@code true} for a continuous restriction
     */
    public SingleRunningCapacity(Set<UUID> nodes, int qty, boolean continuous) {
        super(Collections.<UUID>emptySet(), nodes, continuous);
        this.amount = qty;
    }


    /**
     * Get the amount of resources
     *
     * @return a positive integer
     */
    public int getAmount() {
        return this.amount;
    }

    @Override
    public Sat isSatisfied(Model i) {
        Mapping map = i.getMapping();
        for (UUID n : getInvolvedNodes()) {
            if (map.getRunningVMs(n).size() > amount) {
                return Sat.UNSATISFIED;
            }
        }
        return Sat.SATISFIED;
    }

    @Override
    public Sat isSatisfied(ReconfigurationPlan plan) {
        Model mo = plan.getOrigin();
        if (!isSatisfied(mo).equals(SatConstraint.Sat.SATISFIED)) {
            return Sat.UNSATISFIED;
        }
        mo = plan.getOrigin().clone();

        for (Action a : plan) {
            if (!a.apply(mo)) {
                return Sat.UNSATISFIED;
            }
            if (!isSatisfied(mo).equals(SatConstraint.Sat.SATISFIED)) {
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

        SingleRunningCapacity that = (SingleRunningCapacity) o;
        return getInvolvedNodes().equals(that.getInvolvedNodes())
                && amount == that.amount;
    }

    @Override
    public int hashCode() {
        return 31 * amount + getInvolvedNodes().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("singleRunningCapacity(")
                .append("nodes=").append(getInvolvedNodes())
                .append(", amount=").append(amount);
        if (!isContinuous()) {
            b.append(", discrete");
        } else {
            b.append(", continuous");
        }
        return b.append(")").toString();
    }

    @Override
    public ReconfigurationPlanValidator getValidator() {
        return new Checker();
    }

    /**
     * Checker for the constraint.
     */
    private class Checker extends DefaultReconfigurationPlanValidator {

        private Map<UUID, Integer> usage;

        public Checker() {
            usage = new HashMap<>(getInvolvedNodes().size());
        }

        @Override
        public boolean accept(BootNode a) {
            if (getInvolvedNodes().contains(a.getNode())) {
                usage.put(a.getNode(), 0);
            }
            return true;
        }

        @Override
        public boolean accept(BootVM a) {
            return arrive(a.getDestinationNode());
        }

        @Override
        public boolean accept(KillVM a) {
            return leave(a.getNode());
        }

        @Override
        public boolean accept(MigrateVM a) {
            return leave(a.getSourceNode()) && arrive(a.getDestinationNode());
        }

        @Override
        public boolean accept(ResumeVM a) {
            return arrive(a.getDestinationNode());
        }

        @Override
        public boolean accept(ShutdownVM a) {
            return leave(a.getNode());
        }

        @Override
        public boolean accept(SuspendVM a) {
            return leave(a.getSourceNode());
        }

        private boolean leave(UUID n) {
            if (isContinuous() && getInvolvedNodes().contains(n)) {
                usage.put(n, usage.get(n) - 1);
            }
            return true;
        }

        private boolean arrive(UUID n) {
            if (isContinuous() && getInvolvedNodes().contains(n)) {
                int u = usage.get(n);
                if (u == amount) {
                    return false;
                }
                usage.put(n, u + 1);
            }
            return true;
        }

        @Override
        public boolean acceptResultingModel(Model mo) {
            if (!isContinuous()) {
                Mapping map = mo.getMapping();
                for (UUID n : getInvolvedNodes()) {
                    if (map.getRunningVMs(n).size() > amount) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public boolean acceptOriginModel(Model mo) {
            if (isContinuous()) {
                Mapping map = mo.getMapping();
                for (UUID n : getInvolvedNodes()) {
                    int nb = map.getRunningVMs(n).size();
                    if (nb > amount) {
                        return false;
                    }
                    usage.put(n, nb);
                }
                return true;
            }
            return true;
        }
    }

    private class Checker2 extends DefaultReconfigurationPlanChecker {

        private Map<UUID, Integer> usage;

        public Checker2(Set<UUID> vs, Set<UUID> ns) {
            super(vs, ns);
            usage = new HashMap<>(ns.size());
            srcRunnings = new HashSet<>();
        }

        private boolean leave(UUID n) {
            if (isContinuous() && nodes.contains(n)) {
                usage.put(n, usage.get(n) - 1);
            }
            return true;
        }

        private boolean arrive(UUID n) {
            if (isContinuous() && nodes.contains(n)) {
                int u = usage.get(n);
                if (u == amount) {
                    return false;
                }
                usage.put(n, u + 1);
            }
            return true;
        }

        @Override
        public boolean start(BootNode a) {
            if (nodes.contains(a.getNode())) {
                usage.put(a.getNode(), 0);
            }
            return true;
        }

        @Override
        public boolean start(BootVM a) {
            return arrive(a.getDestinationNode());
        }

        @Override
        public boolean start(KillVM a) {
            if (isContinuous() && srcRunnings.remove(a.getVM())) {
                return leave(a.getNode());
            }
            return true;
        }

        @Override
        public boolean start(MigrateVM a) {
            return leave(a.getSourceNode()) && arrive(a.getDestinationNode());
        }

        @Override
        public boolean start(ResumeVM a) {
            return arrive(a.getDestinationNode());
        }

        @Override
        public boolean start(ShutdownVM a) {
            return leave(a.getNode());
        }

        @Override
        public boolean start(SuspendVM a) {
            return leave(a.getSourceNode());
        }

        private Set<UUID> srcRunnings;

        @Override
        public boolean startsWith(Model mo) {
            if (!isContinuous()) {
                Mapping map = mo.getMapping();
                for (UUID n : nodes) {
                    if (map.getRunningVMs(n).size() > amount) {
                        return false;
                    }
                }
                srcRunnings.addAll(map.getRunningVMs());
            }
            return true;
        }

        @Override
        public boolean endsWith(Model mo) {
            if (isContinuous()) {
                Mapping map = mo.getMapping();
                for (UUID n : nodes) {
                    int nb = map.getRunningVMs(n).size();
                    if (nb > amount) {
                        return false;
                    }
                    usage.put(n, nb);
                }
                return true;
            }
            return true;
        }
    }
}
