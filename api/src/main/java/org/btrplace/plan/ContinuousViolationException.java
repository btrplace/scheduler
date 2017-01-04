/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.event.Action;

/**
 * An exception to signal an action violates a given constraint.
 *
 * @author Fabien Hermenier
 */
public class ContinuousViolationException extends SatConstraintViolationException {

    private final transient Action action;

    /**
     * New constraint.
     *
     * @param cstr   the violated constraint
     * @param action the action that violates the constraint
     */
    public ContinuousViolationException(SatConstraint cstr, Action action) {
        super(cstr, "Constraint '" + cstr + "' is violated by the action '" + action + "'");
        this.action = action;
    }

    public Action getAction() {
        return action;
    }
}
