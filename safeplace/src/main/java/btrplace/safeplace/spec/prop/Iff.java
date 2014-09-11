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

package btrplace.safeplace.spec.prop;

import btrplace.safeplace.verification.spec.SpecModel;

/**
 * @author Fabien Hermenier
 */
public class Iff extends BinaryProp {

    private Or o;


    public Iff(Proposition p1, Proposition p2) {
        super(p1, p2);
        o = new Or(new And(p1, p2), new And(p1.not(), p2.not()));
    }

    @Override
    public String operator() {
        return "<-->";
    }

    @Override
    public And not() {
        return o.not();
    }

    @Override
    public Boolean eval(SpecModel m) {
        return o.eval(m);
    }


    @Override
    public Proposition simplify(SpecModel m) {
        return new Or(new And(p1.simplify(m), p2.simplify(m)), new And(p1.not().simplify(m), p2.not().simplify(m)));
    }
}
