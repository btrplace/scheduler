/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class SetMinus extends Minus<Set> {

    public SetMinus(Term<Set> t1, Term<Set> t2) {
        super(t1, t2);
        if (!a.type().equals(b.type())) {
            throw new IllegalArgumentException("Type mismatch");
        }
    }

    @Override
    public Set<Object> eval(Context mo, Object... args) {
        Collection<?> o1 = a.eval(mo, args);
        Collection<?> o2 = b.eval(mo, args);

        Set<Object> l = new HashSet<>();
        o1.stream().filter(o -> !o2.contains(o)).forEach(l::add);
        return l;
    }
}
