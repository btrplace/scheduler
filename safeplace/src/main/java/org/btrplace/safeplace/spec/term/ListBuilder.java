/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.spec.prop.Proposition;
import org.btrplace.safeplace.spec.type.ListType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Build a set of elements from a variable, a proposition to test which of the values have to be inserted,
 * and a term to perform transformations on the variable.
 *
 * @author Fabien Hermenier
 *         TODO: multiple variables
 */
public class ListBuilder<T> implements Term<List<T>> {

  private final Proposition p;

  private final UserVar<?> v;
  private final Term<T> t;

  private final Type type;

  public ListBuilder(Term<T> t, UserVar<?> v, Proposition p) {
    this.p = p;
    this.t = t;
    this.v = v;
    type = new ListType(t.type());
  }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public List<T> eval(Context mo, Object... args) {
        List<T> res = new ArrayList<>();
        List<Constant> domain = v.domain(mo);
        for (Constant c : domain) {
            mo.setValue(v.label(), c.eval(mo));
            Boolean ok = p.eval(mo);
            if (ok) {
                res.add(t.eval(mo));
            }
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("[").append(t).append(". ");
        b.append(v.pretty());
        if (!p.equals(Proposition.True)) {

            b.append(" , ").append(p);
        }
        return b.append(']').toString();
    }
}
