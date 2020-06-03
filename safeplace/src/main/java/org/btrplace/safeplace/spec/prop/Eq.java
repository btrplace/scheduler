/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * @author Fabien Hermenier
 */
public class Eq extends AtomicProp {

    public Eq(Term a, Term b) {
        super(a, b, "=");
    }

    @Override
    public AtomicProp not() {
        return new NEq(a, b);
    }

    @Override
    public Boolean eval(Context m) {
        Object vA = a.eval(m);
        Object vB = b.eval(m);
        if (vA == null && vB == null) {
            return true;
        }
        return vA != null && vA.equals(vB);
    }
}
