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

import org.btrplace.safeplace.spec.type.SetType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.verification.spec.SpecModel;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class ExplodedSet extends Term<Set> {

    private List<Term> terms;

    private Type t;

    private Set cache;

    public ExplodedSet(List<Term> ts, Type enclType) {
        this.terms = ts;
        t = new SetType(enclType);
    }

    @Override
    public Set eval(SpecModel mo) {
        if (cache == null) {
            cache = new HashSet<>();
            for (Term t : terms) {
                cache.add(t.eval(mo));
            }
        }
        return cache;
    }

    @Override
    public Type type() {
        return t;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("{");
        Iterator ite = terms.iterator();
        if (ite.hasNext()) {
            b.append(ite.next().toString());
        }
        while (ite.hasNext()) {
            b.append(", ").append(ite.next());
        }
        b.append('}');
        return b.toString();
    }

    @Override
    public boolean includes(SpecModel mo, Collection<Object> col) {
        return eval(mo).containsAll(col);
    }

    @Override
    public boolean contains(SpecModel mo, Object o) {
        return eval(mo).contains(o);
    }
}
