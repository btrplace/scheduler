/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace.solver.choco;

import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Collection;
import java.util.List;

/**
 * Utility methods to extract variables from slices.
 *
 * @author Fabien Hermenier
 */
public final class SliceUtils {

    private SliceUtils() {
    }

    public static IntDomainVar[] extractHosters(List<Slice> slices) {
        IntDomainVar[] vs = new IntDomainVar[slices.size()];
        int i = 0;
        for (Slice s : slices) {
            vs[i++] = s.getHoster();
        }
        return vs;
    }

    public static IntDomainVar[] extractStarts(Collection<Slice> slices) {
        IntDomainVar[] vs = new IntDomainVar[slices.size()];
        int i = 0;
        for (Slice s : slices) {
            vs[i++] = s.getStart();
        }
        return vs;
    }

    public static IntDomainVar[] extractEnds(Collection<Slice> slices) {
        IntDomainVar[] vs = new IntDomainVar[slices.size()];
        int i = 0;
        for (Slice s : slices) {
            vs[i++] = s.getEnd();
        }
        return vs;
    }

    public static void linkMoments(ReconfigurationProblem rp, Slice slice) {
        CPSolver s = rp.getSolver();
        s.post(s.eq(slice.getEnd(), s.plus(slice.getDuration(), slice.getStart())));
    }
}
