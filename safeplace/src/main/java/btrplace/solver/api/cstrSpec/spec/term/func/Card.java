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

import btrplace.solver.api.cstrSpec.spec.type.ColType;
import btrplace.solver.api.cstrSpec.spec.type.IntType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Card extends Function<Integer> {

    @Override
    public Type type() {
        return IntType.getInstance();
    }


    @Override
    public Integer eval(SpecModel mo, List<Object> args) {
        Collection c = (Collection) args.get(0);
        if (c == null) {
            return null;
        }
        return c.size();
    }

    @Override
    public String id() {
        return "card";
    }

    @Override
    public Type[] signature() {
        return new Type[]{new ColType(null)};
    }
}
