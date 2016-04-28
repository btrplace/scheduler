/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco;


import org.chocosolver.solver.variables.IntVar;

import java.util.List;

/**
 * Utility methods to extract variables from slices.
 *
 * @author Fabien Hermenier
 */
public final class SliceUtils {

    /**
     * No instantiation.
     */
    private SliceUtils() {
    }

    /**
     * Extract and merge the variables indicating the slices hosts.
     *
     * @param slices the slices
     * @return an array containing every hosting variable
     */
    public static IntVar[] extractHoster(List<Slice> slices) {
        return slices.stream().map(Slice::getHoster).toArray(IntVar[]::new);
    }

    /**
     * Extract and merge the variables indicating the slices consume moment.
     *
     * @param slices the slices
     * @return an array containing every consume variable
     */
    public static IntVar[] extractStarts(List<Slice> slices) {
        return slices.stream().map(Slice::getStart).toArray(IntVar[]::new);
    }

    /**
     * Extract and merge the variables indicating the slices end moment.
     *
     * @param slices the slices
     * @return an array containing every end variable
     */
    public static IntVar[] extractEnds(List<Slice> slices) {
        return slices.stream().map(Slice::getEnd).toArray(IntVar[]::new);
    }

    /**
     * Extract and merge the variables indicating the slices duration.
     *
     * @param slices the slices
     * @return an array containing every variable
     */
    public static IntVar[] extractDurations(List<Slice> slices) {
        return slices.stream().map(Slice::getDuration).toArray(IntVar[]::new);
    }
}
