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

package org.btrplace.safeplace.fuzzer;

import org.btrplace.safeplace.Constraint;
import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.spec.term.UserVar;
import org.btrplace.safeplace.spec.term.Var;
import org.btrplace.safeplace.verification.spec.Context;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class ParamsFuzzer {

    private boolean continuous = true, discrete = true;

    private Random rnd = new Random();
    public ParamsFuzzer() {

    }

    public List<Constant> build(Constraint c, Context mo) {
        if (c.isCore()) {
            return new ArrayList<>();
        }
        List<Constant> l = new ArrayList<>(c.getParameters().size());
        for (UserVar v : c.getParameters()) {
            l.add(new Constant(pick(v, mo), v.type()));
        }
        return l;
    }

    public Object pick(UserVar v, Context mo) {
        //Get the underlying domain
        Var b = (Var) v.getBackend();
        Set s = mo.domain(b.label());
        if (s == null) {
            throw new UnsupportedOperationException("No domain for variable '" + b.label() + "'");
        }
        //TODO: depending on the operator
        return null;
    }

    public Object pickValue(Set s) {
        int n = rnd.nextInt(s.size());
        Iterator it = s.iterator();
        while (n > 0) {
            it.next();
            n--;
        }
        return it.next();
    }

    public Set pickSet(Set in) {
        Set s = new HashSet<>();
        for (Object v : in) {
            if (rnd.nextBoolean()) {
                s.add(v);
            }
        }
        return s;
    }

    public Set pickPart(Set in) {
        List<Set<Object>> p = new ArrayList<>(in.size());
        int c = 2;
        for (Object t : in) {
            int n = rnd.nextInt(c);
            if (n != 0) { //Add in, it denotes its position (index + 1)
                if (n >= p.size()) {
                    Set<Object> h = new HashSet<>();
                    p.add(h);
                    c++;
                }
                p.get(n - 1).add(t);

            }

        }
        return new HashSet<>(p);
    }

    public boolean continuous(Constraint c) {
        //TODO: check fuzzing direction
        Random rnd = new Random();
        boolean conti = rnd.nextBoolean();
        if (!c.isDiscrete()) {
            conti = false;
        }
        return conti;
    }

    public ParamsFuzzer continuous(boolean b) {
        continuous = b;
        return this;
    }

    public ParamsFuzzer discrete(boolean b) {
        discrete = b;
        return this;
    }

    public boolean continuous() {
        return continuous;
    }

    public boolean discrete() {
        return discrete;
    }
}
