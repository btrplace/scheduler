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

package org.btrplace.btrpsl.constraint;

import java.util.Set;

/**
 * A catalog that contains several constraints builder, associated by their name.
 *
 * @author Fabien Hermenier
 */
public interface ConstraintsCatalog {
    /**
     * Get all the available constraints.
     *
     * @return a set of constraint identifier. May be empty
     */
    Set<String> getAvailableConstraints();

    /**
     * Get a placement constraints builder from its identifier.
     *
     * @param id the identifier of the constraint
     * @return the constraints builder or {@code null} if the identifier is unknown
     */
    SatConstraintBuilder getConstraint(String id);
}
