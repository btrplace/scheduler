/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.spec.term.Term;

/**
 * @author Fabien Hermenier
 */
public abstract class AtomicProp implements Proposition {

    protected Term a;
    protected Term b;

  private final String op;

    public AtomicProp(Term a, Term b, String op) {
        this.a = a;
        this.b = b;
        this.op = op;
    }

    @Override
    public String toString() {
        return a.toString() + " " + op + " " + b.toString();
    }
}
