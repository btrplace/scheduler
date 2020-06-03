/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;


/**
 * An exception to signal an error when building a constraint.
 *
 * @author Fabien Hermenier
 */
public class ConstraintBuilderException extends Exception {

    /**
     * A new exception with an error message.
     *
     * @param message the error message
     */
    public ConstraintBuilderException(String message) {
        super(message);
    }

    /**
     * A new exception that rethrown an exception.
     *
     * @param message the error message
     * @param t       the exception to rethrow
     */
    public ConstraintBuilderException(String message, Throwable t) {
        super(message, t);
    }
}
