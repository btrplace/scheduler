/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.model.VM;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.VMEvent;
import org.btrplace.safeplace.spec.type.ActionType;
import org.btrplace.safeplace.spec.type.SetType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.spec.type.VMType;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.HashSet;
import java.util.Set;

/**
 * Get all the actions that manipulate a VM.
 *
 * @author Fabien Hermenier
 */
public class Actions implements Function<Set<Action>> {

    @Override
    public Type type() {
        return new SetType(ActionType.getInstance());
    }


    @Override
    public Set<Action> eval(Context mo, Object... args) {
        VM v = (VM) args[0];
        if (v == null) {
            throw new UnsupportedOperationException();
        }
        Set<Action> s = new HashSet<>();
        for (Action a : mo.getPlan()) {
            if (a instanceof VMEvent) {
                if (((VMEvent) a).getVM().equals(v)) {
                    s.add(a);
                }
            }
        }
        return s;
    }

    @Override
    public String id() {
        return "actions";
    }

    @Override
    public Type[] signature() {
        return new Type[]{VMType.getInstance()};
    }
}
