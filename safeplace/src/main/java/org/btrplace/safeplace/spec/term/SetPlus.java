/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class SetPlus extends Plus<Set<?>> {

    public SetPlus(Term<Set<?>> t1, Term<Set<?>> t2) {
        super(t1, t2);
        if (!a.type().equals(b.type())) {
            throw new IllegalArgumentException("type mismatch");
        }
    }

    @Override
    public Set eval(Context mo, Object... args) {
        Set<?> o1 = a.eval(mo);
        Set<?> o2 = b.eval(mo);
        Set l = new HashSet<>(o1);
        l.addAll(o2);
        return l;
    }
}
