/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;

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
     * @return the transformed parameter. {@code null if the transformation is not possible}
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
