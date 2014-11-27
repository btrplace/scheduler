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

import org.btrplace.safeplace.verification.spec.SpecModel;

/**
 * Logical and between several propositions.
 *
 * @author Fabien Hermenier
 */
public class And extends BinaryProp {

    public And(Proposition p1, Proposition p2) {
        super(p1, p2);
    }

    @Override
    public String operator() {
        return "&";
    }

    @Override
    public Or not() {
        return new Or(p1.not(), p2.not());
    }

    @Override
    public Boolean eval(SpecModel m) {

        Boolean r1 = p1.eval(m);
        if (r1 == null) {
            return null;
        }
        if (!r1) {
            return false;
        }
        Boolean r2 = p2.eval(m);
        if (r2 == null) {
            return null;
        }
        return r2;
    }

    @Override
    public Proposition simplify(SpecModel m) {
        return new And(p1.simplify(m), p2.simplify(m));
    }

    @Override
    public String toString() {
        if (p1 == Proposition.True) {
            return p2.toString();
        }
        if (p2 == Proposition.True) {
            return p1.toString();
        }
        return super.toString();
    }

}