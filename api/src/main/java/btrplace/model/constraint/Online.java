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
import btrplace.plan.DefaultReconfigurationPlanChecker;
import btrplace.plan.ReconfigurationPlanValidator;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.DefaultReconfigurationPlanValidator;
import btrplace.plan.event.ShutdownNode;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * A constraint to force a set of nodes at being online.
 * <p/>
 * The restriction provided by the constraint is discrete.
 * however, if some of the nodes are already offline, then
 * their state will be unchanged.
 *
 * @author Fabien Hermenier
 */
public class Online extends SatConstraint {

    /**
     * Make a new constraint.
     *
     * @param nodes the nodes to set online
     */
    public Online(Set<UUID> nodes) {
        super(Collections.<UUID>emptySet(), nodes, false);
    }

    @Override
    public Sat isSatisfied(Model i) {
        Mapping c = i.getMapping();
        for (UUID n : getInvolvedNodes()) {
            if (!c.getOnlineNodes().contains(n)) {
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

        Online that = (Online) o;
        return getInvolvedNodes().equals(that.getInvolvedNodes());
    }

    @Override
    public int hashCode() {
        return getInvolvedNodes().hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder("online(nodes=")
                .append(getInvolvedNodes())
                .append(", discrete")
                .append(")").toString();
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }

    @Override
    public ReconfigurationPlanValidator getValidator() {
        return new Checker();
    }

    private class Checker extends DefaultReconfigurationPlanValidator {

        @Override
        public boolean accept(ShutdownNode a) {
            return !getInvolvedNodes().contains(a.getNode());
        }

        @Override
        public boolean acceptOriginModel(Model mo) {
            return true;
        }

        @Override
        public boolean acceptResultingModel(Model mo) {
            Mapping c = mo.getMapping();
            for (UUID n : getInvolvedNodes()) {
                if (!c.getOnlineNodes().contains(n)) {
                    return false;
                }
            }
            return true;
        }
    }

    private class Checker2 extends DefaultReconfigurationPlanChecker {

        public Checker2(Set<UUID> vs, Set<UUID> ns) {
            super(vs, ns);
        }

        @Override
        public boolean start(ShutdownNode a) {
            return !nodes.contains(a.getNode());
        }

        @Override
        public boolean endsWith(Model mo) {
            Mapping c = mo.getMapping();
            for (UUID n : nodes) {
                if (!c.getOnlineNodes().contains(n)) {
                    return false;
                }
            }
            return true;
        }
    }
}
