/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.element;

/**
 * Denotes an operand that have to be ignored.
 * This is used to avoid interpretation when an error occurred previously
 *
 * @author Fabien Hermenier
 */
public final class IgnorableOperand extends DefaultBtrpOperand {

    /**
     * Singleton.
     */
    private static final IgnorableOperand SINGLETON = new IgnorableOperand();

    /**
     * Singleton, no instantiation
     */
    private IgnorableOperand() {
    }

    /**
     * Unsupported operation.
     */
    @Override
    public Type type() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    /**
     * @return 0
     */
    @Override
    public int degree() {
        return 0;
    }

    /**
     * Get the unique instance of this class.
     *
     * @return the singleton
     */
    public static IgnorableOperand getInstance() {
        return SINGLETON;
    }


    /**
     * @return {@link #getInstance()}
     */
    @Override
    public BtrpOperand copy() {
        return SINGLETON;
    }
}
