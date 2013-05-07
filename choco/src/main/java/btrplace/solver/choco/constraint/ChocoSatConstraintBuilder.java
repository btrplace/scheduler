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

package btrplace.solver.choco.constraint;

import btrplace.model.constraint.SatConstraint;

/**
 * Interface to specify a builder that create a {@link ChocoSatConstraint} from
 * a specific {@link SatConstraint}.
 *
 * @author Fabien Hermenier
 */
public interface ChocoSatConstraintBuilder {

    /**
     * Get the class of the SatConstraint associated to the builder.
     *
     * @return a Class derived from {@link SatConstraint}
     */
    Class<? extends SatConstraint> getKey();

    /**
     * Build the ChocoConstraint associated to the {@link SatConstraint}
     * identified as key.
     *
     * @param cstr the model constraint
     * @return the associated ChocoConstraint or {@code null} if an error occurred
     */
    ChocoSatConstraint build(SatConstraint cstr);
}
