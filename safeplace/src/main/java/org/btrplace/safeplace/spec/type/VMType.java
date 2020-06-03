/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.type;

import org.btrplace.model.VM;

/**
 * @author Fabien Hermenier
 */
public class VMType implements Atomic {

    private static final VMType instance = new VMType();

    private VMType() {
    }

    public static VMType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "vm";
    }

    @Override
    public String encode() {
        return label();
    }

    @Override
    public Object toJSON(Object value) {
        return ((VM)value).id();
    }

    @Override
    public Object fromJSON(Object value) {
        return new VM((Integer) value);
    }
}
