/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.spec.prop.Not;
import org.btrplace.safeplace.spec.prop.Proposition;
import org.btrplace.safeplace.spec.term.func.Function;
import org.btrplace.safeplace.spec.term.func.FunctionCall;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ConstraintCall extends FunctionCall<Boolean> implements Proposition {

    public ConstraintCall(Function<Boolean> c, List<Term> args) {
        super(c, args, Moment.ANY);
    }

    @Override
    public Proposition not() {
        return new Not(this);
    }

    @Override
    public Boolean eval(Context m) {
        List<Object> ins = new ArrayList<>();
        for (Term t : args()) {
            Object o = t.eval(m);
            ins.add(o);
        }
        m.saveStack();
        Boolean ret = c.eval(m, ins.toArray());
        m.restoreStack();
        return ret;
    }
}
