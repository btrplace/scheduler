/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.runner.staticPartitioning.splitter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public final class Splitters {

    private Splitters() {
    }

    public static <T> Set<T> extractInside(Set<T> s, Set<T> in) {
        Set<T> res = new HashSet<>();
        for (Iterator<T> ite = s.iterator(); ite.hasNext(); ) {
            T v = ite.next();
            if (in.contains(v)) {
                ite.remove();
                res.add(v);
            }
        }
        return res;
    }
}
