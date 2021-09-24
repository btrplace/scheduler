/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

/**
 * Model a virtual machine.
 * VM should not be instantiated directly. Use {@link Model#newVM()} instead.
 *
 * @author Fabien Hermenier
 * @see Model#newVM()
 */
public class VM implements Element {

  private final int id;

    /**
     * The element identifier.
     */
    public static final String TYPE = "vm";

    /**
     * Make a new VM.
     *
     * @param i the VM identifier.
     */
    public VM(int i) {
        this.id = i;
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public String toString() {
        return toString(id);
    }

    /**
     * Helper to stringify a VM.
     *
     * @param id the VM identifier.
     * @return the string version of a VM having the given identifier.
     */
    public static String toString(int id) {
        return TYPE + "#" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VM)) {
            return false;
        }

        VM vm = (VM) o;

        return id == vm.id();
    }

    @Override
    public int hashCode() {
        return id;
    }
}
