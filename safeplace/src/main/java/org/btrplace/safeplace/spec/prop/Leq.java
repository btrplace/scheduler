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
public class Leq extends AtomicProp {

    public Leq(Term a, Term b) {
        super(a, b, "<=");
    }

    @Override
    public AtomicProp not() {
        return new Lt(b, a);
    }

    @Override
    public Boolean eval(Context m) {
        Integer iA = (Integer) a.eval(m);
        Integer iB = (Integer) b.eval(m);
        if (iA == null || iB == null) {
            return null;
        }
        return iA <= iB;
    }

}
