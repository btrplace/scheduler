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

package btrplace.plan;

import btrplace.model.Model;

/**
 * An action to perform on an element and that will alter an instance on success.
 *
 * @author Fabien Hermenier
 */
public abstract class Action {

    private int start;

    private int stop;

    public Action(int st, int ed) {
        this.start = st;
        this.stop = ed;
    }

    /**
     * Apply the action on an instance.
     *
     * @param i the instance to alter with the action
     * @return {@code true} if the action was applied successfully
     */
    public abstract boolean apply(Model i);

    /**
     * Get the moment the action starts.
     *
     * @return a positive integer
     */
    public int getStart() {
        return start;
    }

    /**
     * Get the moment the action ends.
     *
     * @return a positive integer
     */
    public int getEnd() {
        return stop;
    }
}
