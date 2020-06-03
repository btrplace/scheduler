/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.safeplace.spec.type.ColType;
import org.btrplace.safeplace.spec.type.IntType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.Collection;

/**
 * @author Fabien Hermenier
 */
public class Sum implements Function<Integer> {

    @Override
    public Type type() {
        return IntType.getInstance();
    }

    @Override
    public Integer eval(Context mo, Object... args) {
        Collection<Integer> c = (Collection<Integer>) args[0];
        if (c == null) {
            return null;
        }
        int s = 0;
        for (Integer i : c) {
            s += i;
        }
        return s;
    }

    @Override
    public String id() {
        return "sum";
    }

    @Override
    public Type[] signature() {
        return new Type[]{new ColType(IntType.getInstance())};
    }
}
