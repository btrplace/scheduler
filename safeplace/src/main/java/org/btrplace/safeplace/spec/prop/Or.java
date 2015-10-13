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

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.verification.spec.Context;

/**
 * @author Fabien Hermenier
 */
public class Or extends BinaryProp {

    public Or(Proposition p1, Proposition p2) {
        super(p1, p2);
    }

    @Override
    public String operator() {
        return "|";
    }

    @Override
    public And not() {
        return new And(p1.not(), p2.not());
    }

    @Override
    public Boolean eval(Context m) {
        Boolean r1 = p1.eval(m);
        if (r1 == null) {
            return null;
        }
        if (r1) {
            return true;
        }
        return p2.eval(m);
    }

    @Override
    public Proposition simplify(Context m) {
        return new Or(p1.simplify(m), p2.simplify(m));
    }

    @Override
    public String toString() {
        if (p1 == Proposition.False) {
            return p2.toString();
        }
        if (p2 == Proposition.False) {
            return p1.toString();
        }
        return super.toString();
    }
}
