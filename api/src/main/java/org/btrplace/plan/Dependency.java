/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.plan.event.Action;

import java.util.HashSet;
import java.util.Set;

/**
 * Store the dependencies for an action. A dependency is another action
 * that must be executed in an earlier stage to make the original action feasible.
 *
 * @author Fabien Hermenier
 */
public class Dependency {

  private final Action a;

  private final Set<Action> deps;

    /**
     * Make a new dependency.
     *
     * @param action       the action
     * @param dependencies its dependencies.
     */
    public Dependency(Action action, Set<Action> dependencies) {
        a = action;
        deps = new HashSet<>(dependencies);
    }

    /**
     * Get the action.
     *
     * @return an action
     */
    public Action getAction() {
        return a;
    }

    /**
     * Get the actions {@link #getAction()} depends on.
     *
     * @return a set of actions that may be empty.
     */
    public Set<Action> getDependencies() {
        return deps;
    }

    @Override
    public String toString() {
        return deps + " -> " + a;
    }
}
