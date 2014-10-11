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
 * A string element in the language.
 * Support concatenation with numbers and other strings
 *
 * @author Fabien Hermenier
 */
public class BtrpString extends DefaultBtrpOperand {

    private String value;

    /**
     * Make a new element.
     *
     * @param str the value of the element.
     */
    public BtrpString(String str) {
        this.value = str;
    }

    @Override
    public BtrpString clone() {
        return new BtrpString(this.value);
    }

    @Override
    public Type type() {
        return Type.string;
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
