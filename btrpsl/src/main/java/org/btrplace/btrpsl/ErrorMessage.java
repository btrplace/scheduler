/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
  private final int lineNo;

    /**
     * The column number.
     */
    private final int colNo;

  /**
     * The script namespace.
     */
    private String namespace;

    /**
     * The error message.
     */
    private final String message;

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
