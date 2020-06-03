/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.spec.prop.Proposition;
import org.btrplace.safeplace.spec.type.SetType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Build a set of elements from a variable, a proposition to test which of the values have to be inserted,
 * and a term to perform transformations on the variable.
 *
 * @author Fabien Hermenier
 *         TODO: multiple variables
 */
public class SetBuilder<T> implements Term<Set<T>> {

  private final Proposition p;

  private final UserVar<?> v;
  private final Term<T> t;

  private final Type type;

  public SetBuilder(Term<T> t, UserVar<?> v, Proposition p) {
    this.p = p;
    this.t = t;
    this.v = v;
    type = new SetType(t.type());
  }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public Set<T> eval(Context mo, Object... args) {
        Set<T> res = new HashSet<>();
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
        StringBuilder b = new StringBuilder("{").append(t).append(". ");
        b.append(v.pretty());
        if (!p.equals(Proposition.True)) {

            b.append(" , ").append(p);
        }
        return b.append('}').toString();
    }
}
