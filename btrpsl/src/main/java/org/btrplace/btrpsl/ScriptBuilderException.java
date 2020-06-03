/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl;

/**
 * A Exception related to an error while building a script.
 *
 * @author Fabien Hermenier
 */
public class ScriptBuilderException extends Exception {

    private static final long serialVersionUID = 5502255795746863232L;

    private final transient ErrorReporter errReporter;

    /**
     * Make a new exception.
     *
     * @param err the object that store the errors.
     */
    public ScriptBuilderException(ErrorReporter err) {
        super(err.toString());
        errReporter = err;
    }

    /**
     * New exception reporting a simple error.
     *
     * @param msg the error message
     */
    public ScriptBuilderException(String msg) {
        super(msg);
        errReporter = null;
    }

    /**
     * Make an exception that preserve the stack trace.
     *
     * @param msg the error message
     * @param t   the original exception
     */
    public ScriptBuilderException(String msg, Throwable t) {
        super(msg, t);
        errReporter = null;
    }

    /**
     * Get the error reporters.
     *
     * @return the report provided at instantiation, may be {@code null}
     */
    public ErrorReporter getErrorReporter() {
        return this.errReporter;
    }

    @Override
    public String getMessage() {
        if (errReporter != null) {
            return errReporter.toString();
        }
        return super.getMessage();
    }
}
