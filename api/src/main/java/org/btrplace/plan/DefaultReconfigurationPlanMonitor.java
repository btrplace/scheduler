/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.model.Model;
import org.btrplace.plan.event.Action;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

  private final ReconfigurationPlan plan;

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
            curModel = plan.getOrigin().copy();
            pre.clear();
            nbCommitted = 0;
            for (Action a : plan) {
                Set<Action> deps = plan.getDirectDependencies(a);
                if (deps.isEmpty()) {
                    this.dependencies.put(a, new Dependency(a, Collections.emptySet()));
                } else {
                    Dependency dep = new Dependency(a, deps);
                    this.dependencies.put(a, dep);
                    for (Action x : dep.getDependencies()) {
                        pre.putIfAbsent(x, new HashSet<>());
                        Set<Dependency> pres = pre.get(x);
                        pres.add(dep);
                    }
                }
            }
        }
    }

    @Override
    public Model getCurrentModel() {
        Model cpy;
        synchronized (lock) {
            cpy = curModel.copy();
        }
        return cpy;
    }

    @Override
    public Set<Action> commit(Action a) {
        Set<Action> s = new HashSet<>();
        synchronized (lock) {
            boolean ret = a.apply(curModel);
            if (!ret) {
                throw new InfeasibleActionException(curModel, a);
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
        ReconfigurationPlan cpy;
        synchronized (lock) {
            cpy = new DefaultReconfigurationPlan(plan.getOrigin().copy());
            for (Action a : plan) {
                //Cannot clone an action. Sad
                cpy.add(a);
            }
        }
        return cpy;
    }
}
