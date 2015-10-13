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

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.util.AllPackingsGenerator;
import org.btrplace.safeplace.verification.spec.Context;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class NoPackings extends AtomicProp {

    public NoPackings(Term<Set<Set>> a, Term<Set> b) {
        super(a, b, "<<:");
    }

    public Boolean eval(Context ctx) {
        Set left = new HashSet<>();
        Set right = (Set) b.eval(ctx);
        int nb = 0;
        for (Set s : ((Set<Set>) a.eval(ctx))) {
            nb += s.size();
            left.addAll(s);
        }
        //At least on element in left is not in right
        if (!right.containsAll(left)) {
            return true;
        }
        //there is duplicates
        return nb != left.size();
    }

    private Set<Set<Set<Object>>> allPacking(Collection<Object> args) {
        AllPackingsGenerator<Object> pg = new AllPackingsGenerator<>(Object.class, args);
        Set<Set<Set<Object>>> packings = new HashSet<>();
        while (pg.hasNext()) {
            Set<Set<Object>> s = pg.next();
            if (!s.isEmpty()) {
                packings.add(s);
            }
        }
        return packings;
    }

    @Override
    public Packings not() {
        return new Packings(a, b);
    }
}
