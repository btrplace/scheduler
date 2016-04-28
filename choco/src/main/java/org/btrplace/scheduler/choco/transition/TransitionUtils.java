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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
        return l.stream()
                .filter(t -> t.getDSlice() != null)
                .map(VMTransition::getDSlice).collect(Collectors.toList());
    }


    /**
     * Extract all the c-slices of a list of {@link Transition}.
     *
     * @param l the transitions to browse
     * @return a list of c-slices that may be empty
     */
    public static List<Slice> getCSlices(Collection<VMTransition> l) {
        return l.stream()
                .filter(t -> t.getCSlice() != null)
                .map(VMTransition::getCSlice).collect(Collectors.toList());
    }

    /**
     * Extract the consume moments of an array of actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntVar[] getStarts(List<? extends Transition<?>> actions) {
        return actions.stream().map(Transition::getStart).toArray(IntVar[]::new);
    }

    /**
     * Extract the hostingEnd moments of an array of node actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntVar[] getHostingEnds(List<NodeTransition> actions) {
        return actions.stream().map(NodeTransition::getHostingEnd).toArray(IntVar[]::new);
    }

    /**
     * Extract the hostingStart moments of an array of node actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntVar[] getHostingStarts(List<NodeTransition> actions) {
        return actions.stream().map(NodeTransition::getHostingStart).toArray(IntVar[]::new);
    }

    /**
     * Extract the end moments of an array of actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntVar[] getEnds(List<? extends Transition<?>> actions) {
        return actions.stream().map(Transition::getEnd).toArray(IntVar[]::new);
    }

    /**
     * Extract the durations of an array of actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntVar[] getDurations(List<? extends Transition<?>> actions) {
        return actions.stream().map(Transition::getDuration).toArray(IntVar[]::new);
    }
}
