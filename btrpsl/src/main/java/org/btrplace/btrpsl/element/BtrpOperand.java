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
 * Interface specifying an operand with its types and the available operation it may
 * be involved into.
 *
 * @author Fabien Hermenier
 */
public interface BtrpOperand extends Cloneable {

    /**
     * The possible operand type.
     */
    public static enum Type {
        /**
         * Denotes a virtual machine
         */VM,
        /**
         * Denotes a hosting node
         */node,
        /**
         * Denotes a constant integer
         */number,
        /**
         * Denotes a constant string
         */string,
    }

    /**
     * Compute the opposite of the operand.
     *
     * @return the opposite operand
     */
    BtrpOperand not();

    /**
     * Get the type of the operand.
     *
     * @return a type
     */
    Type type();

    /**
     * Get the degree of the operand.
     * A degree of 0 indicates a single value. A degree of 1 indicates a set of single value.
     * A degree of 2 indicate a set of set of values. Basically. An operand with a degree n is
     * a set of elements with all having the degree n - 1
     *
     * @return a positive integer
     */
    int degree();

    /**
     * Get the label of the operand.
     * An operand with a label is a variable.
     *
     * @return a String if the operand is a variable. {@code null} otherwise
     */
    String label();

    /**
     * Set the label of the operand.
     * The operand will then be considered as a variable.
     *
     * @param lbl the label to use
     */
    void setLabel(String lbl);

    /**
     * Computes the power of this operand.
     *
     * @param nb the power coefficient. Must be strictly positive
     * @return the result of the operation
     */
    BtrpOperand power(BtrpOperand nb);

    /**
     * Computes the addition of this operand with another one.
     *
     * @param other the other operand
     * @return the result of the addition
     */
    BtrpOperand plus(BtrpOperand other);

    /**
     * Computes the difference between this operand and another one.
     *
     * @param other the other operand
     * @return the result of the difference
     */
    BtrpOperand minus(BtrpOperand other);

    /**
     * Computes the negation of this operand.
     *
     * @return the result of the negation
     */
    BtrpOperand negate();

    /**
     * Multiply this operand by another one
     *
     * @param other the other operand
     * @return the result of the negation
     */
    BtrpOperand times(BtrpOperand other);

    /**
     * Divides this operand by another one.
     *
     * @param other the other operand
     * @return the result of the division
     */
    BtrpOperand div(BtrpOperand other);

    /**
     * Computes the remainder of this operand division by another one
     *
     * @param other the other operand
     * @return the remainder of the division
     */
    BtrpOperand remainder(BtrpOperand other);

    /**
     * Check if this operand is equals to another one.
     *
     * @param other the operand to compare to
     * @return {@link BtrpNumber#TRUE} if both operand are equals, {@link BtrpNumber#FALSE} otherwise
     */
    BtrpNumber eq(BtrpOperand other);

    /**
     * Check if this operand is greater or equals to another one.
     *
     * @param other the operand to compare to
     * @return {@link BtrpNumber#TRUE} if this operand is greater or equals, {@link BtrpNumber#FALSE} otherwise
     */
    BtrpNumber geq(BtrpOperand other);

    /**
     * Check if this operand is strictly greater than another one.
     *
     * @param other the operand to compare to
     * @return {@link BtrpNumber#TRUE} if this operand is strictly greater, {@link BtrpNumber#FALSE} otherwise
     */
    BtrpNumber gt(BtrpOperand other);

    /**
     * Deep copy of the operand.
     *
     * @return a copy of this operand
     */
    BtrpOperand clone();

    /**
     * Pretty textual representation of the element type.
     *
     * @return a String
     */
    String prettyType();
}
