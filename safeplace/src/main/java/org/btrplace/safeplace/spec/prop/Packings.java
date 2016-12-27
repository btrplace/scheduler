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

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Packings extends AtomicProp {

    public Packings(Term<Set<Set>> a, Term<Set> b) {
        super(a, b, "<<:");
    }

    @Override
    public Boolean eval(Context ctx) {
        //All the sets in a belongs to b and no duplicates
        Set left = new HashSet<>();
        Set right = (Set) b.eval(ctx);
        int nb = 0;
        for (Set s : ((Set<Set>) a.eval(ctx))) {
            nb += s.size();
            left.addAll(s);
            //s is a subset of right
            if (!right.containsAll(s)) {
                return false;
            }
        }
        //and there is no duplicates
        return nb == left.size();
    }

    @Override
    public NoPackings not() {
        return new NoPackings(a, b);
    }
}
