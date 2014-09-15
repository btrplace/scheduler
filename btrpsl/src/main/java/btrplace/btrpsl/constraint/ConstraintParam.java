/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

package btrplace.btrpsl.constraint;

import btrplace.btrpsl.element.BtrpOperand;
import btrplace.btrpsl.tree.BtrPlaceTree;

/**
 * A parameter for a constraint.
 *
 * @author Fabien Hermenier
 */
public interface ConstraintParam<E> {

    /**
     * Get the signature of the parameter without the parameter name.
     *
     * @return a non-empty string
     */
    String prettySignature();

    /**
     * Get the signature of the parameter including the parameter name.
     *
     * @return a non-empty string
     */
    String fullSignature();

    /**
     * Transform an operand into the right btrplace parameter.
     *
     * @param cb   the associated constraint
     * @param tree the tree use to propagate errors
     * @param op   the operand to transform
     * @return the transformed parameter.
     */
    E transform(SatConstraintBuilder cb, BtrPlaceTree tree, BtrpOperand op);

    /**
     * Check if a given operand is compatible with this parameter.
     *
     * @param t the tree used to propagate errors
     * @param o the operand to test
     * @return {@code true} iff the operand is compatible
     */
    boolean isCompatibleWith(BtrPlaceTree t, BtrpOperand o);

    /**
     * Get the parameter name.
     *
     * @return a non-empty string
     */
    String getName();
}
