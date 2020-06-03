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
public class Packings extends AtomicProp {

    public Packings(Term<Set<Set>> a, Term<Set> b) {
        super(a, b, "<<:");
    }

    @Override
    public Boolean eval(Context ctx) {
        //All the sets in a belongs to b and no duplicates
        Set<?> left = new HashSet<>();
        Set<?> right = (Set<?>) b.eval(ctx);
        int nb = 0;
        for (Set s : (Set<Set>) a.eval(ctx)) {
            nb += s.size();
            left.addAll(s);
            //s is a subset of right
            if (!right.containsAll(s)) {
                return false;
            }
        }
        //and there is no duplicates
        return nb == left.size();
    }

    @Override
    public NoPackings not() {
        return new NoPackings(a, b);
    }
}
