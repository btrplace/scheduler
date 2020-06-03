/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class NoPackings extends AtomicProp {

    public NoPackings(Term<Set<Set>> a, Term<Set> b) {
        super(a, b, "<<:");
    }

    @Override
    public Boolean eval(Context ctx) {
        Set<Set<?>> left = new HashSet<>();
        Set<?> right = (Set<?>) b.eval(ctx);
        int nb = 0;
        for (Set<Set<?>> s : (Set<Set>) a.eval(ctx)) {
            nb += s.size();
            left.addAll(s);
        }
        //At least on element in left is not in right
        if (!right.containsAll(left)) {
            return true;
        }
        //there is duplicates
        return nb != left.size();
    }

    @Override
    public Packings not() {
        return new Packings(a, b);
    }
}
