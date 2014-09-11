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

package btrplace.solver.api.cstrSpec.verification.btrplace;

import btrplace.model.constraint.SatConstraint;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Constraint2BtrPlace {

    public static SatConstraint build(Constraint cstr, List<Constant> params) throws ClassNotFoundException {
        return build("btrplace.model.constraint", cstr, params);
    }

    public static SatConstraint build(String pkg, Constraint cstr, List<Constant> params) throws ClassNotFoundException {
        String clName = cstr.id().substring(0, 1).toUpperCase() + cstr.id().substring(1);
        Class<SatConstraint> cl = (Class<SatConstraint>) Class.forName(pkg + "." + clName);
        List<Object> values = new ArrayList<>(params.size());
        for (Constant c : params) {
            values.add(c.eval(null));
        }
        for (Constructor c : cl.getConstructors()) {
            if (c.getParameterTypes().length == values.size()) {
                try {
                    return (SatConstraint) c.newInstance(values.toArray());
                } catch (Exception e) {
                    //We want ot try other constructors that may match
                }
            }
        }

        throw new IllegalArgumentException("No constructors compatible with values '" + values + "'");
    }
}
