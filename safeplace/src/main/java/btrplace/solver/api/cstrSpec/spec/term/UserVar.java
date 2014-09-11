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

package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.util.AllTuplesGenerator;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
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
        return incl ? backend.type().inside() : backend.type();
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

    public List<Constant> domain(SpecModel mo) {
        Collection col = null;
        col = backend.eval(mo);
        if (incl) {
            List<Constant> s = new ArrayList<>();
            for (Object o : col) {
                s.add(new Constant(o, type()));
            }
            return s;
        } else {
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
        }
    }
}
