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

package btrplace.safeplace.spec.term;

import btrplace.safeplace.spec.type.Type;
import btrplace.safeplace.util.AllTuplesGenerator;
import btrplace.safeplace.verification.spec.SpecModel;
import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class UserVar<T> extends Var<T> {

    private Term<Collection> backend;

    private boolean incl;

    private boolean not;

    public UserVar(String lbl, boolean incl, boolean not, Term backend) {
        super(lbl);
        this.incl = incl;
        this.backend = backend;
        this.not = not;
    }

    @Override
    public Type type() {
        return incl ? backend.type() : backend.type().inside();
    }

    @Override
    public String pretty() {
        return label() + (not ? " /" : " ") + (incl ? ": " : "<: ") + backend;
    }

    public Term<Collection> getBackend() {
        return backend;
    }

    @Override
    public T eval(SpecModel m) {
        return (T) m.getValue(label());
    }

    @Override
    public Constant pickIn(SpecModel mo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object pickIncluded(SpecModel mo) {
        throw new UnsupportedOperationException();
    }

    public Object pick(SpecModel mo) {
        if (incl) {
            return backend.pickIncluded(mo);
        } else {
            return backend.pickIn(mo);
        }
    }

    public List<Constant> domain(SpecModel mo) {
        Collection col = null;
        col = backend.eval(mo);
        if (incl) {
            List<Object> s = new ArrayList<>();
            for (Object o : col) {
                s.add(o);
            }
            List<List<Object>> tuples = new ArrayList<>();
            for (Object o : s) {
                tuples.add(s);
            }
            AllTuplesGenerator<Object> tg = new AllTuplesGenerator<>(Object.class, tuples);
            Set<Constant> res = new HashSet<>();
            for (Object[] tuple : tg) {
                res.add(new Constant(new HashSet(Arrays.asList(tuple)), backend.type()));
            }
            return new ArrayList<>(res);
        } else {
            List<Constant> s = new ArrayList<>();
            for (Object o : col) {
                s.add(new Constant(o, type()));
            }
            return s;
        }
    }

    @Override
    public boolean contains(SpecModel mo, Object o) {
        Collection col = backend.eval(mo);
        return col.contains(o);
    }

    @Override
    public boolean includes(SpecModel mo, Collection<Object> col) {
        Collection c = backend.eval(mo);
        return c.containsAll(col);
    }
}
