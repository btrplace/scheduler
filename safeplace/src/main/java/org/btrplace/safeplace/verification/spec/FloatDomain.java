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

package org.btrplace.safeplace.verification.spec;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class FloatDomain implements Domain<Double> {

    private Set<Double> dom;

    private int lb, ub;

    private double inc;

    public FloatDomain(int lb, int ub, double inc) {
        dom = new HashSet<>();
        for (double i = lb; i <= ub; i += inc) {
            dom.add(i);
        }
        this.lb = lb;
        this.ub = ub;
        this.inc = inc;
    }

    @Override
    public Set<Double> values() {
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

    public FloatDomain clone() {
        return new FloatDomain(lb, ub, inc);
    }

}