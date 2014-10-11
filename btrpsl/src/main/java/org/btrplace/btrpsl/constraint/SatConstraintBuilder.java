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

import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;
import org.btrplace.model.constraint.SatConstraint;

import java.util.List;

/**
 * An interface to specify a generic PlacementConstraint builder.
 *
 * @author Fabien Hermenier
 */
public interface SatConstraintBuilder {

    /**
     * Get the identifier of the constraint.
     *
     * @return a string
     */
    String getIdentifier();

    /**
     * Get the signature of the constraint.
     *
     * @return a string
     */
    String getSignature();

    /**
     * Get the full signature of the constraint, including the parameter name.
     *
     * @return a string
     */
    String getFullSignature();

    /**
     * Get the constraint parameters.
     *
     * @return a non-empty array.
     */
    ConstraintParam[] getParameters();

    /**
     * Build the constraint
     *
     * @param t      the current token.
     * @param params the parameters of the constraint.
     * @return the constraint
     */
    List<SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> params);
}
