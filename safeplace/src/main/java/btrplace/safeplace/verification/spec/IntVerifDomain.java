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

package btrplace.safeplace.verification.spec;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class IntVerifDomain implements VerifDomain<Integer> {

    private Set<Integer> dom;

    private int lb, ub;

    public IntVerifDomain(int lb, int ub) {
        dom = new HashSet<>(ub - lb + 1);
        for (int i = lb; i <= ub; i++) {
            dom.add(i);
        }
        this.lb = lb;
        this.ub = ub;
    }

    @Override
    public Set<Integer> domain() {
        return dom;
    }

    @Override
    public String type() {
        return "int";
    }

    @Override
    public String toString() {
        return lb + ".." + ub;
    }
}
