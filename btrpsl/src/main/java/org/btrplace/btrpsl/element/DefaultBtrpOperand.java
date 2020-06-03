/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.element;

/**
 * An abstract operand.
 *
 * @author Fabien Hermenier
 */
public abstract class DefaultBtrpOperand implements BtrpOperand {

    private String label = null;

    @Override
    public String label() {
        return label;
    }

    @Override
    public void setLabel(String lbl) {
        this.label = lbl;
    }


    @Override
    public BtrpOperand not() {
        throw new UnsupportedOperationException("operator '!' unsupported");
    }


    @Override
    public BtrpOperand power(BtrpOperand nb) {
        throw new UnsupportedOperationException("operator '^' unsupported");
    }

    @Override
    public BtrpOperand plus(BtrpOperand other) {
        throw new UnsupportedOperationException("operator '+' unsupported");
    }

    @Override
    public BtrpOperand minus(BtrpOperand other) {
        throw new UnsupportedOperationException("operator '-' unsupported");
    }

    @Override
    public BtrpOperand negate() {
        throw new UnsupportedOperationException("operator '-' unsupported");
    }

    @Override
    public BtrpOperand times(BtrpOperand other) {
        throw new UnsupportedOperationException("operator '*' unsupported");
    }

    @Override
    public BtrpOperand div(BtrpOperand other) {
        throw new UnsupportedOperationException("operator '/' unsupported");
    }

    @Override
    public BtrpOperand remainder(BtrpOperand other) {
        throw new UnsupportedOperationException("operator '%' unsupported");
    }

    @Override
    public BtrpNumber eq(BtrpOperand other) {
        throw new UnsupportedOperationException("operator '==' unsupported");
    }

    @Override
    public BtrpNumber geq(BtrpOperand other) {
        throw new UnsupportedOperationException("operator '>=' unsupported");
    }

    @Override
    public BtrpNumber gt(BtrpOperand other) {
        throw new UnsupportedOperationException("operator '>' unsupported");
    }

    /**
     * Pretty textual representation of the element type.
     *
     * @return a String
     */
    @Override
    public String prettyType() {
        return prettyType(degree(), type());
    }

    /**
     * Generate a pretty type for an operand.
     *
     * @param o the operand
     * @return a string that describes the operand type.
     */
    public static String prettyType(BtrpOperand o) {
        if (o == IgnorableOperand.getInstance()) {
            return "??";
        }
        return prettyType(o.degree(), o.type());
    }

    /**
     * Pretty textual representation of a given element type.
     *
     * @param degree 0 for a literal, 1 for a set, 2 for a set of sets, ...
     * @param t      the literal
     * @return a String
     */
    public static String prettyType(int degree, Type t) {
        StringBuilder b = new StringBuilder();
        for (int i = degree; i > 0; i--) {
            b.append("set<");
        }
        b.append(t.toString().toLowerCase());
        for (int i = 0; i < degree; i++) {
            b.append(">");
        }
        return b.toString();
    }
}
