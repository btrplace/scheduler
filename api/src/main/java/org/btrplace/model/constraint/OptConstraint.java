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

package org.btrplace.model.constraint;

/**
 * Optimization oriented constraint.
 * Such a constraint cannot be violated. It just asks to minimize or minimize
 * a value.
 *
 * @author Fabien Hermenier
 */
public abstract class OptConstraint implements Constraint {

    /**
     * Get the constraint identifier.
     *
     * @return a non-empty String
     */
    public abstract String id();

    @Override
    public int hashCode() {
        return id().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null && this.getClass().equals(obj.getClass()));
    }

    @Override
    public String toString() {
        return id();
    }
}
