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

package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.util.AllPackingsGenerator;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Packings extends Function<Set> {

    @Override
    public Type type() {
        return new SetType(new SetType(null));
    }

    public Set eval(SpecModel mo, List<Object> args) {
        return allPacking((Collection) args.get(0));
    }


    private Set<Set<Set<Object>>> allPacking(Collection<Object> args) {
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
}
