/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json;

/**
 * An exception related to a conversion process.
 *
 * @author Fabien Hermenier
 */
public class JSONConverterException extends Exception {

    /**
     * Make a new exception.
     *
     * @param msg the error message
     */
    public JSONConverterException(String msg) {
        super(msg);
    }

    /**
     * Make a new exception.
     *
     * @param msg the error message
     * @param t   the root exception
     */
    public JSONConverterException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Rethrow an existing exception.
     *
     * @param t the exception to rethrow
     */
    public JSONConverterException(Throwable t) {
        super(t);
    }

    /**
     * State a node has already been declared.
     *
     * @param id the node identifier
     * @return the generated exception
     */
    public static JSONConverterException nodeAlreadyDeclared(int id) {
        return new JSONConverterException("Node '" + id + "' already declared");
    }

    /**
     * State a VM has already been declared.
     *
     * @param id the VM identifier
     * @return the generated exception
     */
    public static JSONConverterException vmAlreadyDeclared(int id) {
        return new JSONConverterException("VM '" + id + "' already declared");
    }

}
