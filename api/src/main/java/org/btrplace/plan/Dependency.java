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

    private Action a;

    private Set<Action> deps;

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
