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

package btrplace.safeplace.spec.term.func;

import btrplace.safeplace.spec.term.Term;
import btrplace.safeplace.spec.type.SetType;
import btrplace.safeplace.spec.type.Type;
import btrplace.safeplace.util.AllPackingsGenerator;
import btrplace.safeplace.verification.spec.SpecModel;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class Packings extends Function<Set> {

    private static Random rnd = new Random();

    @Override
    public Type type() {
        return new SetType(new SetType(null));
    }

    public Set eval(SpecModel mo, List<Object> args) {
        return allPacking((Collection) args.get(0));
    }


    private Set<Set<Set<Object>>> allPacking(Collection<Object> args) {
        //System.out.println("All packings for " + args);
        AllPackingsGenerator<Object> pg = new AllPackingsGenerator<>(Object.class, args);
        //System.out.println(args);
        Set<Set<Set<Object>>> packings = new HashSet<>();
        while (pg.hasNext()) {
            Set<Set<Object>> s = pg.next();
            if (!s.isEmpty()) {
                packings.add(s);
            }
        }
        //System.out.println(packings);
        return packings;
    }

    @Override
    public String id() {
        return "packings";
    }

    @Override
    public Type[] signature() {
        return new Type[]{new SetType(null)};
    }

    @Override
    public Type type(List<Term> args) {
        return new SetType(new SetType(args.get(0).type()));
    }

    @Override
    public Object pickIn(SpecModel mo, List<Term> args) {
        Collection col = (Collection) args.get(0).eval(mo);
        List<Set<Object>> p = new ArrayList<>(col.size());
        int c = 2;
        for (Object t : ((Collection) args.get(0).eval(mo))) {
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
        return new HashSet<Set<Object>>(p);
        //return p;
    }
}
