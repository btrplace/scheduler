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

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.spec.type.ColType;
import org.btrplace.safeplace.spec.type.ListType;
import org.btrplace.safeplace.spec.type.SetType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Lists implements Function<List> {

    @Override
    public Type type() {
        return new SetType(new ListType(null));
    }


    @Override
    public List eval(Context mo, Object... args) {
        Collection c = (Collection) args[0];
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
        return new SetType(new ListType(((ColType) args.get(0).type()).inside()));
    }

}
