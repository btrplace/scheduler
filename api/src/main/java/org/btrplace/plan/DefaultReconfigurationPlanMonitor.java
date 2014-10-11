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
 * Simulated execution of a {@link ReconfigurationPlan}.
 * The execution relies on the dependencies between the actions, retrieved using
 * {@link ReconfigurationPlan#getDirectDependencies(org.btrplace.plan.event.Action)}.
 * <p>
 * The dependencies are updated each time an action is committed, which means the action
 * have been successfully executed.
 * <p>
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlanMonitor implements ReconfigurationPlanMonitor {

    private ReconfigurationPlan plan;

    private Model curModel;

    private final Map<Action, Set<Dependency>> pre;

    private final Map<Action, Dependency> dependencies;

    private final Object lock;

    private int nbCommitted;

    /**
     * Make a new monitor.
     *
     * @param p the plan to execute
     */
    public DefaultReconfigurationPlanMonitor(ReconfigurationPlan p) {
        this.plan = p;

        pre = new HashMap<>();
        dependencies = new HashMap<>();
        lock = new Object();
        reset();
    }

    private void reset() {
        synchronized (lock) {
            curModel = plan.getOrigin().clone();
            pre.clear();
            nbCommitted = 0;
            for (Action a : plan) {
                Set<Action> deps = plan.getDirectDependencies(a);
                if (deps.isEmpty()) {
                    this.dependencies.put(a, new Dependency(a, Collections.<Action>emptySet()));
                } else {
                    Dependency dep = new Dependency(a, deps);
                    this.dependencies.put(a, dep);
                    for (Action x : dep.getDependencies()) {
                        Set<Dependency> pres = pre.get(x);
                        if (pres == null) {
                            pres = new HashSet<>();
                            pre.put(x, pres);
                        }
                        pres.add(dep);
                    }
                }
            }
        }
    }

    @Override
    public Model getCurrentModel() {
        return curModel;
    }

    @Override
    public Set<Action> commit(Action a) {
        Set<Action> s = new HashSet<>();
        synchronized (lock) {
            boolean ret = a.apply(curModel);
            if (!ret) {
                return null;
            }
            nbCommitted++;
            //Browse all its dependencies for the action
            Set<Dependency> deps = pre.get(a);
            if (deps != null) {
                for (Dependency dep : deps) {
                    Set<Action> actions = dep.getDependencies();
                    actions.remove(a);
                    if (actions.isEmpty()) {
                        Action x = dep.getAction();
                        s.add(x);
                    }
                }
            }
        }
        return s;
    }

    @Override
    public int getNbCommitted() {
        synchronized (lock) {
            return nbCommitted;
        }
    }

    @Override
    public boolean isBlocked(Action a) {
        synchronized (lock) {
            return !dependencies.get(a).getDependencies().isEmpty();
        }
    }

    @Override
    public ReconfigurationPlan getReconfigurationPlan() {
        return plan;
    }
}
