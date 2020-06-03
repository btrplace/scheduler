/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.model.Model;
import org.btrplace.plan.event.Action;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

/**
 * Default implementation for {@link ReconfigurationPlan}.
 * By default, the instance relies on a {@link TimeBasedPlanApplier} to check for the plan applicability.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlan implements ReconfigurationPlan {

  private final Model src;

  private final Set<Action> actions;

    private DependenciesExtractor depsExtractor;

  private static final Comparator<Action> startFirstComparator = new TimedBasedActionComparator(true, true);

    private ReconfigurationPlanApplier applier = new TimeBasedPlanApplier();

  private static final Comparator<Action> sorter = (o1, o2) -> {
    int diffStart = o1.getStart() - o2.getStart();
    if (diffStart == 0) {
      return o1.getEnd() - o2.getEnd();
    }
    return diffStart;
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
        l.sort(sorter);
        StringJoiner joiner = new StringJoiner("\n");
        for (Action a : l) {
            joiner.add(String.format("%d:%d %s", a.getStart(), a.getEnd(), a.toString()));
        }
        return joiner.toString();
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
        return actions.equals(op.getActions()) && src.equals(op.getOrigin());
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
