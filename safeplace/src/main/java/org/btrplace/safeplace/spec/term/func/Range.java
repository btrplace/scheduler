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

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.safeplace.spec.type.IntType;
import org.btrplace.safeplace.spec.type.ListType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.verification.spec.SpecModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Get all the indexes of a given list.
 *
 * @author Fabien Hermenier
 */
public class Range extends Function<List<Integer>> {

    @Override
    public Type type() {
        return new ListType(IntType.getInstance());
    }


    @Override
    public List<Integer> eval(SpecModel mo, List<Object> args) {
        List c = (List) args.get(0);
        if (c == null) {
            return null;
        }
        List<Integer> res = new ArrayList<>(c.size());
        for (int i = 0; i < c.size(); i++) {
            res.add(i);
        }
        return res;
    }

    @Override
    public String id() {
        return "range";
    }

    @Override
    public Type[] signature() {
        return new Type[]{new ListType(null)};
    }
}
