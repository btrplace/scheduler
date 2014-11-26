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
 * An error message
 *
 * @author Fabien Hermenier
 */
public class ErrorMessage {

    /**
     * The line number.
     */
    private int lineNo;

    /**
     * The column number.
     */
    private int colNo;

    /**
     * The script namespace.
     */
    private String namespace;

    /**
     * The error message.
     */
    private String message;

    /**
     * Build a new error message.
     *
     * @param l   the pointed line number
     * @param c   the pointed column number
     * @param msg the error message
     */
    public ErrorMessage(int l, int c, String msg) {
        this(null, l, c, msg);
    }

    /**
     * Build a new error message.
     *
     * @param ns  the namespace for the error message.
     * @param l   the pointed line number
     * @param c   the pointed column number
     * @param msg the error message
     */
    public ErrorMessage(String ns, int l, int c, String msg) {
        this.namespace = ns;
        this.lineNo = l;
        this.colNo = c;
        this.message = msg;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(message.length() + 15);
        b.append('[');

        if (namespace != null) {
            b.append(namespace);
        }
        b.append(' ').append(lineNo).append(':').append(colNo).append("] ").append(message);
        return b.toString();
    }

    /**
     * The line number.
     *
     * @return positive int
     */
    public int lineNo() {
        return lineNo;
    }

    /**
     * The column number.
     *
     * @return positive int
     */
    public int colNo() {
        return colNo;
    }

    /**
     * The script namespace.
     *
     * @return non-empty string
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Get the error namespace.
     *
     * @param s the new namespace
     */
    public void setNamespace(String s) {
        namespace = s;
    }

    /**
     * Get the error message.
     *
     * @return a message
     */
    public String message() {
        return message;
    }
}
