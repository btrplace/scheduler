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

package org.btrplace.btrpsl.element;

/**
 * Denotes an operand that have to be ignored.
 * This is used to avoid interpretation when an error occurred previously
 *
 * @author Fabien Hermenier
 */
public final class IgnorableOperand extends DefaultBtrpOperand implements Cloneable {

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
    public BtrpOperand clone() {
        return SINGLETON;
    }
}
