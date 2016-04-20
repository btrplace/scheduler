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

package org.btrplace.scheduler.choco.transition;

import org.btrplace.scheduler.choco.Slice;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utility class to extract members of a collection of {@link Transition}.
 *
 * @author Fabien Hermenier
 */
public final class TransitionUtils {

    private TransitionUtils() {
    }

    /**
     * Extract all the d-slices of a list of {@link Transition}.
     *
     * @param l the transitions to browse
     * @return a list of d-slices that may be empty
     */
    public static List<Slice> getDSlices(Collection<VMTransition> l) {
        List<Slice> slices = new ArrayList<>();
        for (VMTransition m : l) {
            if (m.getDSlice() != null) {
                slices.add(m.getDSlice());
            }
        }
        return slices;

    }


    /**
     * Extract all the c-slices of a list of {@link Transition}.
     *
     * @param l the transitions to browse
     * @return a list of c-slices that may be empty
     */
    public static List<Slice> getCSlices(Collection<VMTransition> l) {
        List<Slice> slices = new ArrayList<>();
        for (VMTransition m : l) {
            if (m.getCSlice() != null) {
                slices.add(m.getCSlice());
            }
        }
        return slices;
    }

    /**
     * Extract the consume moments of an array of actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntVar[] getStarts(List<? extends Transition> actions) {
        IntVar[] starts = new IntVar[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            starts[i] = actions.get(i).getStart();
        }
        return starts;
    }

    /**
     * Extract the hostingEnd moments of an array of node actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntVar[] getHostingEnds(List<NodeTransition> actions) {
        IntVar[] starts = new IntVar[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            starts[i] = actions.get(i).getHostingEnd();
        }
        return starts;
    }

    /**
     * Extract the hostingStart moments of an array of node actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntVar[] getHostingStarts(List<NodeTransition> actions) {
        IntVar[] starts = new IntVar[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            starts[i] = actions.get(i).getHostingStart();
        }
        return starts;
    }

    /**
     * Extract the end moments of an array of actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntVar[] getEnds(List<? extends Transition> actions) {
        IntVar[] starts = new IntVar[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            starts[i] = actions.get(i).getEnd();
        }
        return starts;
    }

    /**
     * Extract the durations of an array of actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntVar[] getDurations(List<? extends Transition> actions) {
        IntVar[] starts = new IntVar[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            starts[i] = actions.get(i).getDuration();
        }
        return starts;
    }
}
