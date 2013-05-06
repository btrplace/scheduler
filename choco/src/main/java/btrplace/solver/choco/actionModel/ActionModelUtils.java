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

package btrplace.solver.choco.actionModel;

import btrplace.solver.choco.Slice;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to extract members of a collection of {@link btrplace.solver.choco.actionModel.ActionModel}.
 *
 * @author Fabien Hermenier
 */
public final class ActionModelUtils {

    private ActionModelUtils() {
    }

    /**
     * Extract all the d-slices of a list of {@link btrplace.solver.choco.actionModel.ActionModel}.
     *
     * @param models the models to browse
     * @return a list of d-slices that may be empty
     */
    public static List<Slice> getDSlices(VMActionModel[] models) {
        List<Slice> slices = new ArrayList<>();
        for (VMActionModel m : models) {
            if (m.getDSlice() != null) {
                slices.add(m.getDSlice());
            }
        }
        return slices;
    }

    /**
     * Extract all the d-slices of a list of {@link btrplace.solver.choco.actionModel.ActionModel}.
     *
     * @param l the models to browse
     * @return a list of d-slices that may be empty
     */
    public static List<Slice> getDSlices(List<VMActionModel> l) {
        return getDSlices(l.toArray(new VMActionModel[l.size()]));
    }

    /**
     * Extract all the c-slices of a list of {@link btrplace.solver.choco.actionModel.ActionModel}.
     *
     * @param models the models to browse
     * @return a list of c-slices that may be empty
     */
    public static List<Slice> getCSlices(VMActionModel[] models) {
        List<Slice> slices = new ArrayList<>();
        for (VMActionModel m : models) {
            if (m.getCSlice() != null) {
                slices.add(m.getCSlice());
            }
        }
        return slices;
    }

    /**
     * Extract all the c-slices of a list of {@link btrplace.solver.choco.actionModel.ActionModel}.
     *
     * @param l the models to browse
     * @return a list of c-slices that may be empty
     */
    public static List<Slice> getCSlices(List<VMActionModel> l) {
        return getCSlices(l.toArray(new VMActionModel[l.size()]));
    }

    /**
     * Extract the consume moments of an array of actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntDomainVar[] getStarts(ActionModel[] actions) {
        IntDomainVar[] starts = new IntDomainVar[actions.length];
        for (int i = 0; i < actions.length; i++) {
            starts[i] = actions[i].getStart();
        }
        return starts;
    }

    /**
     * Extract the hostingEnd moments of an array of node actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntDomainVar[] getHostingEnds(NodeActionModel[] actions) {
        IntDomainVar[] starts = new IntDomainVar[actions.length];
        for (int i = 0; i < actions.length; i++) {
            starts[i] = actions[i].getHostingEnd();
        }
        return starts;
    }

    /**
     * Extract the hostingStart moments of an array of node actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntDomainVar[] getHostingStarts(NodeActionModel[] actions) {
        IntDomainVar[] starts = new IntDomainVar[actions.length];
        for (int i = 0; i < actions.length; i++) {
            starts[i] = actions[i].getHostingStart();
        }
        return starts;
    }

    /**
     * Extract the end moments of an array of actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntDomainVar[] getEnds(ActionModel[] actions) {
        IntDomainVar[] starts = new IntDomainVar[actions.length];
        for (int i = 0; i < actions.length; i++) {
            starts[i] = actions[i].getEnd();
        }
        return starts;
    }

    /**
     * Extract the durations of an array of actions.
     * The ordering is maintained
     *
     * @return an array of variable
     */
    public static IntDomainVar[] getDurations(ActionModel[] actions) {
        IntDomainVar[] starts = new IntDomainVar[actions.length];
        for (int i = 0; i < actions.length; i++) {
            starts[i] = actions[i].getDuration();
        }
        return starts;
    }
}
