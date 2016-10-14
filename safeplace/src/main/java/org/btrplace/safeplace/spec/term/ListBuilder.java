/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

    private Proposition p;

    private UserVar v;
    private Term<T> t;

    private Type type;

    public ListBuilder(Term<T> t, UserVar v, Proposition p) {
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
        List res = new ArrayList();
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
