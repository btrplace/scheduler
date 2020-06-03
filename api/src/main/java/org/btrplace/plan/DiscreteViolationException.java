/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

    private final transient Model mo;

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
