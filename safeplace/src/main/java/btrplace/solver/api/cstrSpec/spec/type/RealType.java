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

package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.solver.api.cstrSpec.spec.term.Constant;

/**
 * @author Fabien Hermenier
 */
public class RealType extends Atomic {

    private double inf, sup;

    private static final RealType instance = new RealType(0, 5);

    private RealType(int lb, int ub) {
        this.inf = lb;
        this.sup = ub;
    }

    public static RealType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean match(String n) {
        try {
            Double i = Double.parseDouble(n);
            if (i >= inf && i <= sup) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public String label() {
        return "real";
    }

    @Override
    public Constant newValue(String n) {
        return new Constant(Double.parseDouble(n), RealType.getInstance());
    }

    public Constant newValue(double i) {
        return new Constant(i, RealType.getInstance());
    }

    @Override
    public boolean comparable(Type t) {
        return t.equals(NoneType.getInstance()) || equals(t);
    }

}
