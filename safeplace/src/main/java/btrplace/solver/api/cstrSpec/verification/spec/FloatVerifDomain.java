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

package btrplace.solver.api.cstrSpec.verification.spec;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class FloatVerifDomain implements VerifDomain<Double> {

    private Set<Double> dom;

    private int lb, ub;

    public FloatVerifDomain(int lb, int ub, double inc) {
        dom = new HashSet<>();
        for (double i = lb; i <= ub; i += inc) {
            dom.add(i);
        }
        this.lb = lb;
        this.ub = ub;
    }

    @Override
    public Set<Double> domain() {
        return dom;
    }

    @Override
    public String type() {
        return "float";
    }

    @Override
    public String toString() {
        return lb + ".." + ub;
    }
}
