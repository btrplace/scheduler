/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.Collection;

/**
 * @author Fabien Hermenier
 */
public class Inc extends AtomicProp {

    public Inc(Term<?> a, Term<?> b) {
        super(a, b, "<:");
    }

    @Override
    public AtomicProp not() {
        return new NInc(a, b);
    }

    @Override
    public Boolean eval(Context m) {
        Collection<?> o = (Collection<?>) a.eval(m);
        Collection<?> c = (Collection<?>) b.eval(m);
        if (o != null && c != null) {
            return c.containsAll(o);
        }
        return null;
    }

}
