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
 * A notification can be attached to an action to inform the action executor
 * that non-critical operations can be performed. A hook indicates the moment
 * the notification can be considered. Typically, before or after the action
 * execution.
 *
 * @author Fabien Hermenier
 */
public interface Notification {

    /**
     * Apply the notification on a given model.
     *
     * @param m the model to modify
     * @return {@code true} iff the modification succeeded
     */
    boolean apply(Model m);
}
