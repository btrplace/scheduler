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

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to extract members of a collection of {@link ActionModel}.
 *
 * @author Fabien Hermenier
 */
public class ActionModelUtil {

    private ActionModelUtil() {
    }

    /**
     * Extract all the d-slices of a list of {@link ActionModel}.
     *
     * @param models the models to browse
     * @return a list of d-slices that may be empty
     */
    public static List<Slice> getDSlices(ActionModel[] models) {
        List<Slice> slices = new ArrayList<Slice>();
        for (ActionModel m : models) {
            if (m.getDSlice() != null) {
                slices.add(m.getDSlice());
            }
        }
        return slices;
    }

    /**
     * Extract all the c-slices of a list of {@link ActionModel}.
     *
     * @param models the models to browse
     * @return a list of c-slices that may be empty
     */
    public static List<Slice> getCSlices(ActionModel[] models) {
        List<Slice> slices = new ArrayList<Slice>();
        for (ActionModel m : models) {
            if (m.getCSlice() != null) {
                slices.add(m.getCSlice());
            }
        }
        return slices;
    }
}
