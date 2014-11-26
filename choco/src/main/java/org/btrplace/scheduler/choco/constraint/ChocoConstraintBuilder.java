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

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.constraint.Constraint;

/**
 * Interface to specify a builder that create a {@link ChocoConstraint} from
 * a specific {@link org.btrplace.model.constraint.Constraint}.
 *
 * @author Fabien Hermenier
 */
public interface ChocoConstraintBuilder {

    /**
     * Get the class of the Constraint associated to the builder.
     *
     * @return a Class derived from {@link Constraint}
     */
    Class<? extends Constraint> getKey();

    /**
     * Build the ChocoConstraint associated to the {@link Constraint}
     * identified as key.
     *
     * @param cstr the model constraint
     * @return the associated ChocoConstraint or {@code null} if an error occurred
     */
    ChocoConstraint build(Constraint cstr);
}
