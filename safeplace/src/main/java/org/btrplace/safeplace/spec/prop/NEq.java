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
public class NEq extends AtomicProp {

    public NEq(Term a, Term b) {
        super(a, b, "/=");
    }

    @Override
    public AtomicProp not() {
        return new Eq(a, b);
    }

    @Override
    public Boolean eval(Context mo) {
        Object vA = a.eval(mo);
        Object vB = b.eval(mo);
        if ((vA == null && vB != null) || (vA != null && vB == null)) {
            return true;
        }
        return vA != null && !vA.equals(vB);
    }
}
