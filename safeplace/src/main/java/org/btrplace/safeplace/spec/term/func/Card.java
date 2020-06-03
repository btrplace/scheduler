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
public class Card implements Function<Integer> {

    @Override
    public Type type() {
        return IntType.getInstance();
    }


    @Override
    public Integer eval(Context mo, Object... args) {
        Collection<?> c = (Collection<?>) args[0];
        if (c == null) {
            return null;
        }
        return c.size();
    }

    @Override
    public String id() {
        return "card";
    }

    @Override
    public Type[] signature() {
        return new Type[]{new ColType(null)};
    }
}
