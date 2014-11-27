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

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.verification.spec.SpecModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class FunctionCall<T> extends Term<T> {

    private Function<T> c;

    private List<Term> args;

    public static enum Moment {
        begin {
            @Override
            public String toString() {
                return "^";
            }
        },
        end {
            @Override
            public String toString() {
                return "$";
            }
        },
        any {
            @Override
            public String toString() {
                return "";
            }
        }
    }

    private Moment moment;

    public FunctionCall(Function<T> c, List<Term> args, Moment m) {
        check(c, args);
        this.c = c;
        this.args = args;
        this.moment = m;
    }

    @Override
    public Type type() {
        return c.type(args);
    }

    @Override
    public T eval(SpecModel m) {
        List<Object> values = new ArrayList<>(args.size());
        for (Term t : args) {
            values.add(t.eval(m));
        }
        return c.eval(m, values);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(moment);
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

    private static void check(Function f, List<Term> args) {
        Type[] expected = f.signature(args);
        if (expected.length != args.size()) {
            throw new IllegalArgumentException(toString(f.id(), args) + " cannot match " + f);
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(args.get(i).type())) {
                throw new IllegalArgumentException(toString(f.id(), args) + " cannot match " + f);
            }
        }
    }

    public static String toString(String id, List<Term> args) {
        StringBuilder b = new StringBuilder(id);
        b.append('(');
        Iterator<Term> ite = args.iterator();
        while (ite.hasNext()) {
            b.append(ite.next().type());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    @Override
    public Object pickIn(SpecModel mo) {
        return c.pickIn(mo, args);
    }

    @Override
    public Object pickIncluded(SpecModel mo) {
        return c.pickIncluded(mo, args);
    }

    @Override
    public boolean isConstant() {
        boolean cons = true;
        for (Term t : args) {
            cons &= t.isConstant();
        }
        return cons;
    }

    @Override
    public boolean contains(SpecModel mo, Object o) {
        List<Object> values = new ArrayList<>();
        for (Term t : args) {
            values.add(t.eval(mo));
        }
        return c.contains(mo, values, o);
    }

    @Override
    public boolean includes(SpecModel mo, Collection<Object> col) {
        List<Object> values = new ArrayList<>();
        for (Term t : args) {
            values.add(t.eval(mo));
        }
        return c.containsAll(mo, values, col);

    }
}
