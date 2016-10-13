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

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * @author Fabien Hermenier
 */
public class IntMinus extends Minus<Integer> {

    public IntMinus(Term<Integer> t1, Term<Integer> t2) {
        super(t1, t2);
    }

    @Override
    public Integer eval(Context mo, Object... args) {
        Integer o1 = a.eval(mo);
        Integer o2 = b.eval(mo);
        if (o1 == null || o2 == null) {
            return null;
        }
        return o1 - o2;
    }
}
