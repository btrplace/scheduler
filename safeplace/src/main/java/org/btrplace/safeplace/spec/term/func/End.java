/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.plan.event.Action;
import org.btrplace.safeplace.spec.type.ActionType;
import org.btrplace.safeplace.spec.type.IntType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * Get the moment an action ends.
 *
 * @author Fabien Hermenier
 */
public class End implements Function<Integer> {

    @Override
    public IntType type() {
        return IntType.getInstance();
    }

    @Override
    public Integer eval(Context mo, Object... args) {
        Action a = (Action) args[0];
        if (a == null) {
            throw new UnsupportedOperationException();
        }
        return a.getEnd();
    }

    @Override
    public String id() {
        return "end";
    }

    @Override
    public Type[] signature() {
        return new Type[]{ActionType.getInstance()};
    }
}
