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

package org.btrplace.btrpsl;

/**
 * A Exception related to an error while building a script.
 *
 * @author Fabien Hermenier
 */
public class ScriptBuilderException extends Exception {

    private static final long serialVersionUID = 5502255795746863232L;

    private ErrorReporter errReporter;

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
    }

    /**
     * Make an exception that preserve the stack trace.
     *
     * @param msg the error message
     * @param t   the original exception
     */
    public ScriptBuilderException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Get the error reporters.
     *
     * @return the report provided at instantiation
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
