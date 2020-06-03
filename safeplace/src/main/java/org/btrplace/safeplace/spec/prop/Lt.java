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
public class Lt extends AtomicProp {

    public Lt(Term a, Term b) {
        super(a, b, "<");
    }

    @Override
    public AtomicProp not() {
        return new Lt(b, a);
    }

    @Override
    public Boolean eval(Context m) {
        Integer vA = (Integer) a.eval(m);
        Integer vB = (Integer) b.eval(m);
        if (vA == null || vB == null) {
            return null;
        }
        return vA < vB;
    }
}
