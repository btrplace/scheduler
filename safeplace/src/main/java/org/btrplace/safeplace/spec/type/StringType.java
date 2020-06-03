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
public class StringType implements Litteral, Atomic {

  private static final StringType instance = new StringType();

    @Override
    public Constant parse(String n) {
        return new Constant(n, this);
    }

    @Override
    public String toString() {
        return "string";
    }

    public static StringType getInstance() {
        return instance;
    }

    @Override
    public String encode() {
        return label();
    }

    @Override
    public Object toJSON(Object value) {
        return value.toString();
    }

    @Override
    public Object fromJSON(Object value) {
        return value.toString();
    }
}
