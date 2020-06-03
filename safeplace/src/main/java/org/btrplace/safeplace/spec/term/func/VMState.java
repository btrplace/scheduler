/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.model.VM;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.spec.type.VMStateType;
import org.btrplace.safeplace.spec.type.VMType;
import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * @author Fabien Hermenier
 */
public class VMState implements Function<VMStateType.Type> {

    @Override
    public VMStateType type() {
        return VMStateType.getInstance();
    }

    @Override
    public VMStateType.Type eval(Context mo, Object... args) {
        VM v = (VM) args[0];
        if (v == null) {
            throw new UnsupportedOperationException();
        }
        return mo.getMapping().state(v);
    }

    @Override
    public String id() {
        return "vmState";
    }

    @Override
    public Type[] signature() {
        return new Type[]{VMType.getInstance()};
    }
}
