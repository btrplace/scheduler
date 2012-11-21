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

import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Collection;

/**
 * Utility methods to extract variables from slices.
 *
 * @author Fabien Hermenier
 */
public final class SliceUtils {

    private SliceUtils() {
    }

    /**
     * Extract and merge the variables indicating the slices hosts.
     *
     * @param slices the slices
     * @return an array containing every hosting variable
     */
    public static IntDomainVar[] extractHosters(Collection<Slice> slices) {
        IntDomainVar[] vs = new IntDomainVar[slices.size()];
        int i = 0;
        for (Slice s : slices) {
            vs[i++] = s.getHoster();
        }
        return vs;
    }

    /**
     * Extract and merge the variables indicating the slices start moment.
     *
     * @param slices the slices
     * @return an array containing every start variable
     */
    public static IntDomainVar[] extractStarts(Collection<Slice> slices) {
        IntDomainVar[] vs = new IntDomainVar[slices.size()];
        int i = 0;
        for (Slice s : slices) {
            vs[i++] = s.getStart();
        }
        return vs;
    }

    /**
     * Extract and merge the variables indicating the slices end moment.
     *
     * @param slices the slices
     * @return an array containing every end variable
     */
    public static IntDomainVar[] extractEnds(Collection<Slice> slices) {
        IntDomainVar[] vs = new IntDomainVar[slices.size()];
        int i = 0;
        for (Slice s : slices) {
            vs[i++] = s.getEnd();
        }
        return vs;
    }
}
