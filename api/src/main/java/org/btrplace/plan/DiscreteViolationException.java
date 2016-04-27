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

import org.btrplace.model.Model;
import org.btrplace.model.constraint.SatConstraint;

/**
 * An exception to signal a model violates a given constraint.
 *
 * @author Fabien Hermenier
 */
public class DiscreteViolationException extends SatConstraintViolationException {

    private final Model mo;

    /**
     * New constraint.
     *
     * @param cstr the violated constraint
     * @param mo   the model that violates the constraint
     */
    public DiscreteViolationException(SatConstraint cstr, Model mo) {
        super(cstr, "Constraint '" + cstr + "' is violated by the following model:\n" + mo);
        this.mo = mo;
    }

    /**
     * Get the model that violates the constraint
     *
     * @return a model
     */
    public Model getModel() {
        return mo;
    }
}
