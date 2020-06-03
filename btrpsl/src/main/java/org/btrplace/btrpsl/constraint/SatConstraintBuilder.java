/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
    ConstraintParam<?>[] getParameters();

    /**
     * Build the constraint
     *
     * @param t      the current token.
     * @param params the parameters of the constraint.
     * @return the constraint
     */
    List<? extends SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> params);
}
