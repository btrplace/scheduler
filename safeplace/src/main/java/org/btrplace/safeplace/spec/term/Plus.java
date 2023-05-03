/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.spec.type.Type;

/**
 * @author Fabien Hermenier
 */
public abstract class Plus<T> implements Term<T> {

    protected Term<T> a;
    protected Term<T> b;

    protected Plus(Term<T> t1, Term<T> t2) {
        this.a = t1;
        this.b = t2;
    }

    @Override
    public String toString() {
        return a.toString() + " + " + b.toString();
    }

    @Override
    public Type type() {
        return a.type();
    }
}
