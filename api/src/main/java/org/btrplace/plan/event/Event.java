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

package org.btrplace.plan.event;

import org.btrplace.model.Model;

/**
 * A event to apply on a model to modify it.
 * See the {@link Action} class for a time-bounded event.
 *
 * @author Fabien Hermenier
 * @see Action
 */
public interface Event {

    /**
     * Apply the event on a given model.
     *
     * @param m the model to modify
     * @return {@code true} iff the modification succeeded
     */
    boolean apply(Model m);


    /**
     * Notify a visitor to visit the action.
     *
     * @param v the visitor to notify
     */
    Object visit(ActionVisitor v);
}
