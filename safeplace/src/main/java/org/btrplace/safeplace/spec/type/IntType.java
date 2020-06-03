/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.type;

import org.btrplace.safeplace.spec.term.Constant;

/**
 * @author Fabien Hermenier
 */
public class IntType implements Litteral, Atomic {

    private static final IntType instance = new IntType();

    private IntType() {
    }

    public static IntType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "int";
    }

    @Override
    public Constant parse(String n) {
        return new Constant(Integer.parseInt(n), IntType.getInstance());
    }

    @Override
    public Object toJSON(Object value) {
        return value;
    }

    @Override
    public String encode() {
        return toString();
    }

    @Override
    public Integer fromJSON(Object value) {
        return (Integer) value;
    }

}
