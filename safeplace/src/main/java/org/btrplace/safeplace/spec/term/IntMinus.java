/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * @author Fabien Hermenier
 */
public class IntMinus extends Minus<Integer> {

    public IntMinus(Term<Integer> t1, Term<Integer> t2) {
        super(t1, t2);
    }

    @Override
    public Integer eval(Context mo, Object... args) {
        Integer o1 = a.eval(mo);
        Integer o2 = b.eval(mo);
        if (o1 == null || o2 == null) {
            return null;
        }
        return o1 - o2;
    }
}
