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

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class SetMinus extends Minus<Set> {

    public SetMinus(Term<Set> t1, Term<Set> t2) {
        super(t1, t2);
        if (!a.type().equals(b.type())) {
            throw new RuntimeException();
        }
    }

    @Override
    public Set eval(Context mo, Object... args) {
        Collection o1 = a.eval(mo);
        Collection o2 = b.eval(mo);
        Set l = new HashSet();
        o1.stream().filter(o -> !o2.contains(o)).forEach(l::add);
        return l;
    }
}
