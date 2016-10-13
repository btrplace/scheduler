/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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

import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.spec.prop.Not;
import org.btrplace.safeplace.spec.prop.Proposition;
import org.btrplace.safeplace.spec.term.func.FunctionCall;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ConstraintCall implements Proposition {

    private Constraint c;

    private List<Term> args;

    public ConstraintCall(Constraint c, List<Term> args) {
        check(c, args);
        this.c = c;
        this.args = args;
    }

    @Override
    public Proposition not() {
        return new Not(this);
    }

    @Override
    public Boolean eval(Context m) {
        List<UserVar> ps = c.args();
        List<Object> ins = new ArrayList<>(ps.size());
        for (Term t : args) {
            Object o = t.eval(m);
            ins.add(o);
        }
        m.saveStack();
        Boolean ret = c.eval(m, ins);
        m.restoreStack();
        return ret;
    }

    private static void check(Constraint f, List<Term> args) {
        Type[] expected = f.signature();
        if (expected.length != args.size()) {
            throw new IllegalArgumentException(FunctionCall.toString(f.id(), args) + " cannot match " + f.toString());
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(args.get(i).type())) {
                throw new IllegalArgumentException(FunctionCall.toString(f.id(), args) + " cannot match " + f.toString());
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(c.id()).append('(');
        Iterator<Term> ite = args.iterator();
        while (ite.hasNext()) {
            b.append(ite.next().toString());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    @Override
    public Proposition simplify(Context m) {
        return this;
    }
}
