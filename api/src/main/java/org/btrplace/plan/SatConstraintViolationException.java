/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.model.constraint.SatConstraint;

/**
 * Exception that notifies a constraint violation.
 *
 * @author Fabien Hermenier
 */
public class SatConstraintViolationException extends Exception {

    private final transient SatConstraint cstr;

    /**
     * Declare a new exception.
     *
     * @param cstr the violated constraint
     * @param msg  the error message
     */
    public SatConstraintViolationException(SatConstraint cstr, String msg) {
        super(msg);
        this.cstr = cstr;
    }

    /**
     * Get the violated constraint
     *
     * @return a constraint
     */
    public SatConstraint getConstraint() {
        return cstr;
    }
}
