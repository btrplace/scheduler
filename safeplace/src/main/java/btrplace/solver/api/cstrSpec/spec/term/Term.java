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

package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public abstract class Term<T> {

    public abstract T eval(SpecModel mo);

    public abstract Type type();

    public UserVar newInclusive(String n, boolean not) {
        if (type() instanceof Primitive) {
            return null;
        }
        return new UserVar(n, true, not, this);
    }

    public UserVar<Set> newPart(String n, boolean not) {
        return new UserVar(n, false, not, this);
    }

}
