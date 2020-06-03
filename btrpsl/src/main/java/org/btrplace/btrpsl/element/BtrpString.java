/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.element;

/**
 * A string element in the language.
 * Support concatenation with numbers and other strings
 *
 * @author Fabien Hermenier
 */
public class BtrpString extends DefaultBtrpOperand {

  private final String value;

    /**
     * Make a new element.
     *
     * @param str the value of the element.
     */
    public BtrpString(String str) {
        this.value = str;
    }

    @Override
    public BtrpString copy() {
        return new BtrpString(this.value);
    }

    @Override
    public Type type() {
        return Type.STRING;
    }

    @Override
    public int degree() {
        return 0;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        BtrpString x = (BtrpString) o;
        return x.value.equals(value);

    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public BtrpNumber eq(BtrpOperand other) {
        if (other.getClass() != this.getClass()) {
            return BtrpNumber.FALSE;
        }
        BtrpString x = (BtrpString) other;
        return x.value.equals(value) ? BtrpNumber.TRUE : BtrpNumber.FALSE;
    }

    @Override
    public BtrpString plus(BtrpOperand o) {
        if (o instanceof BtrpSet) {
            throw new UnsupportedOperationException("Unable to append a '" + o.prettyType() + "' to a '" + o.prettyType() + "'");
        }
        return new BtrpString(this.value + o.toString());
    }
}
