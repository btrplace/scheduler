/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.btrpsl.constraint;


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
