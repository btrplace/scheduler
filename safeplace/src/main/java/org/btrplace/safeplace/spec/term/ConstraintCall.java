/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
