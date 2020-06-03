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
public class BoolType implements Litteral, Atomic {


  private static final BoolType instance = new BoolType();

    private BoolType() {
    }

    public static BoolType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "bool";
    }

    @Override
    public String label() {
        return "bool";
    }

    @Override
    public Constant parse(String n) {
        return new Constant(Boolean.parseBoolean(n), BoolType.getInstance());
    }

    public Constant newValue(boolean i) {
        return new Constant(i, BoolType.getInstance());
    }

}
