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
import org.btrplace.plan.event.EventCommittedListener;

/**
 * An object to simulate the application of
 * a plan. The result will be a new model.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationPlanApplier {

    /**
     * Add a listener that will be notified upon events termination.
     *
     * @param l the listener to add
     */
    void addEventCommittedListener(EventCommittedListener l);

    /**
     * Remove a listener.
     *
     * @param l the listener
     * @return {@code true} iff the listener is removed
     */
    boolean removeEventCommittedListener(EventCommittedListener l);

    /**
     * Apply a plan.
     *
     * @param p the plan to apply
     * @return the resulting model if the application succeed. {@code null} otherwise
     */
    Model apply(ReconfigurationPlan p);

    /**
     * Textual representation of a plan.
     *
     * @param p the plan to stringify
     * @return the formatted string
     */
    String toString(ReconfigurationPlan p);
}
