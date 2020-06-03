/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * @author Fabien Hermenier
 */
public class Mult implements Term {

  private final Term a;
  private final Term b;

  public Mult(Term t1, Term t2) {
    this.a = t1;
    this.b = t2;
  }

  @Override
  public Object eval(Context mo, Object... args) {
    Object o1 = a.eval(mo);
    Object o2 = b.eval(mo);
        if (o1 == null || o2 == null) {
            return null;
        }
        if (o1 instanceof Integer) {
          return o1 * o2;
        }
        throw new IllegalArgumentException("Unsupported operation on '" + o1.getClass().getSimpleName() + "'");
    }

    @Override
    public String toString() {
        return a.toString() + " * " + b.toString();
    }

    @Override
    public Type type() {
        return a.type();
    }
}
