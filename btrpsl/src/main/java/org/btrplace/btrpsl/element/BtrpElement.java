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


import org.btrplace.model.Element;

/**
 * Denotes either a VM or a node.
 * An element as a unique name but also a unique UUID that will be used by btrplace.
 *
 * @author Fabien Hermenier
 */
public class BtrpElement extends DefaultBtrpOperand implements Cloneable {

    private String name;

    private Element e;

    private Type t;

    /**
     * Make a new element.
     *
     * @param ty the element type. Either {@link Type#VM} or {@link Type#node}.
     * @param n  the element name
     * @param el the associated BtrPlace element
     */
    public BtrpElement(Type ty, String n, Element el) {
        this.name = n;
        this.t = ty;
        this.e = el;
    }

    /**
     * Get the element identifier.
     *
     * @return a non null String.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the element.
     *
     * @return an element
     */
    public Element getElement() {
        return e;
    }

    /**
     * Check the equality of two elements.
     * Both are equals if they are an instance of a same class and if they contains
     * the same element.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BtrpElement that = (BtrpElement) o;
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * @return {@code 0}
     */
    @Override
    public int degree() {
        return 0;
    }

    @Override
    public Type type() {
        return t;
    }

    @Override
    public BtrpNumber eq(BtrpOperand other) {
        if (this.equals(other)) {
            return BtrpNumber.TRUE;
        }
        return BtrpNumber.FALSE;
    }

    @Override
    public BtrpElement clone() {
        return new BtrpElement(t, name, e);
    }

    @Override
    public String toString() {
        return getName();
    }
}
