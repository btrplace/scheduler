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
