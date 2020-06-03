/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.template;

/**
 * An exception that occurs when a virtual machine can not be built.
 *
 * @author Fabien Hermenier
 */
public class ElementBuilderException extends Exception {

    /**
     * Make an exception with an error message.
     *
     * @param msg the error message
     */
    public ElementBuilderException(String msg) {
        super(msg);
    }

    /**
     * Make an exception with an error message and an exception to re-thrown
     *
     * @param msg the error message
     * @param t   the exception to rethrow
     */
    public ElementBuilderException(String msg, Throwable t) {
        super(msg, t);
    }
}
