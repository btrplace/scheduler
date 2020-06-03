/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.spec.type.ColType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.List;

/**
 * Get the value at a given index of a list.
 *
 * @author Fabien Hermenier
 */
public class ValueAt implements Term {

  private final Term<List<?>> arr;
  private final Term<Integer> idx;

  public ValueAt(Term<List<?>> arr, Term<Integer> idx) {
    this.arr = arr;
    this.idx = idx;
  }

  @Override
  public Type type() {
    return ((ColType) arr.type()).inside();
  }

    @Override
    public Object eval(Context mo, Object... args) {
        List<?> l = arr.eval(mo);
        if (l == null) {
            return null;
        }
        Integer i = idx.eval(mo);
        if (i == null) {
            return null;
        }
        return l.get(i);
    }

    @Override
    public String toString() {
        return arr.toString() + "[" + idx.toString() + "]";
    }
}
