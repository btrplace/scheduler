/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.element;


import org.btrplace.model.Element;

/**
 * Denotes either a VM or a node.
 * An element as a unique name but also a unique UUID that will be used by btrplace.
 *
 * @author Fabien Hermenier
 */
public class BtrpElement extends DefaultBtrpOperand {

  private final String name;

  private final Element e;

  private final Type t;

    /**
     * Make a new element.
     *
     * @param ty the element type. Either {@link org.btrplace.btrpsl.element.BtrpOperand.Type#VM} or {@link org.btrplace.btrpsl.element.BtrpOperand.Type#NODE}.
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
    public BtrpElement copy() {
        return new BtrpElement(t, name, e);
    }

    @Override
    public String toString() {
        return getName();
    }
}
