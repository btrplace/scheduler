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

package org.btrplace.plan;

import org.btrplace.model.Model;
import org.btrplace.plan.event.Action;

import java.util.*;

/**
 * Default implementation for {@link ReconfigurationPlan}.
 * By default, the instance relies on a {@link TimeBasedPlanApplier} to check for the plan applicability.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlan implements ReconfigurationPlan {

    private Model src;

    private Set<Action> actions;

    private DependenciesExtractor depsExtractor;

    private static Comparator<Action> startFirstComparator = new TimedBasedActionComparator(true, true);

    private ReconfigurationPlanApplier applier = new TimeBasedPlanApplier();

    private Comparator<Action> sorter = new Comparator<Action>() {

        public int compare(Action o1, Action o2) {
            int diffStart = o1.getStart() - o2.getStart();
            if (diffStart == 0) {
                return o1.getEnd() - o2.getEnd();
            }
            return diffStart;
        }
    };

    /**
     * Make a new plan that starts from a given model.
     *
     * @param m the source model
     */
    public DefaultReconfigurationPlan(Model m) {
        this.src = m;
        this.actions = new HashSet<>();
        //Dependency management is performed lazily.
        this.depsExtractor = null;
    }

    @Override
    public Model getOrigin() {
        return src;
    }

    @Override
    public boolean add(Action a) {
        boolean ret = this.actions.add(a);
        if (ret && depsExtractor != null) {
            //We only track dependencies incrementally if already started
            a.visit(depsExtractor);
        }
        return ret;
    }

    @Override
    public int getSize() {
        return actions.size();
    }

    @Override
    public int getDuration() {
        int m = 0;
        for (Action a : actions) {
            if (a.getEnd() > m) {
                m = a.getEnd();
            }
        }
        return m;
    }

    @Override
    public Set<Action> getActions() {
        return actions;
    }

    /**
     * Iterate over the actions.
     * The action are automatically sorted increasingly by their starting moment.
     *
     * @return an iterator.
     */
    @Override
    public Iterator<Action> iterator() {
        Set<Action> sorted = new TreeSet<>(startFirstComparator);
        sorted.addAll(actions);
        return sorted.iterator();
    }

    @Override
    public Model getResult() {
        return applier.apply(this);
    }

    @Override
    public String toString() {
        List<Action> l = new ArrayList<>(actions);
        Collections.sort(l, sorter);
        StringBuilder b = new StringBuilder();
        for (Action a : l) {
            b.append(a.getStart()).append(':').append(a.getEnd()).append(' ').append(a.toString()).append('\n');
        }
        return b.toString();
    }

    @Override
    public boolean isApplyable() {
        return applier.apply(this) != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReconfigurationPlan op = (ReconfigurationPlan) o;
        return (actions.equals(op.getActions()) && src.equals(op.getOrigin()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, actions);
    }

    @Override
    public Set<Action> getDirectDependencies(Action a) {
        if (depsExtractor == null) {
            //Track dependencies of all the already registered actions
            depsExtractor = new DependenciesExtractor(src);
            for (Action x : actions) {
                x.visit(depsExtractor);
            }
        }
        return depsExtractor.getDependencies(a);
    }

    @Override
    public ReconfigurationPlanApplier getReconfigurationApplier() {
        return applier;
    }

    @Override
    public void setReconfigurationApplier(ReconfigurationPlanApplier ra) {
        this.applier = ra;
    }
}
