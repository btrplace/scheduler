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
import btrplace.safeplace.spec.type.ListType;
import btrplace.safeplace.spec.type.SetType;
import btrplace.safeplace.spec.type.Type;
import btrplace.safeplace.verification.spec.SpecModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Lists extends Function<java.util.List> {

    @Override
    public Type type() {
        return new SetType(new ListType(null));
    }


    @Override
    public java.util.List eval(SpecModel mo, List<Object> args) {
        Collection c = (Collection) args.get(0);
        if (c == null) {
            return null;
        }
        List l = new ArrayList<>();
        l.add(new ArrayList<>(c));
        return l;
    }

    @Override
    public String id() {
        return "lists";
    }

    @Override
    public Type[] signature() {
        return new Type[]{new SetType(null)};
    }

    @Override
    public Type type(List<Term> args) {
        return new SetType(new ListType(args.get(0).type().inside()));
    }

}
