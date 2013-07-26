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

package btrplace.plan;

import btrplace.plan.event.Action;

import java.util.Comparator;

/**
 * A comparator to sort the actions in the increasing order of their starting moment.
 * If several actions starts at the same moment, the action that ends first in considered.
 * <p/>
 * It is possible to indicate the comparator to differentiate two actions that are simultaneous but not equals.
 * This is meaningful to sort set of actions as actions that are equals
 * wrt. to the comparator will be removed from the set while being different wrt. their {@code equals()} method.
 *
 * @author Fabien Hermenier
 */
public class TimedBasedActionComparator implements Comparator<Action> {

    private boolean diffSimultaneous = false;

    private boolean startBased = true;

    /**
     * New comparator that does not differentiate
     * simultaneous actions.
     */
    public TimedBasedActionComparator() {
        this(true, false);
    }

    /**
     * New comparator.
     *
     * @param onStart         to compare the actions using their start moment. Otherwise, the comparison
     *                        is made wrt. the ending moment
     * @param diffSamePeriods {@code true} to differentiate simultaneous actions.
     */
    public TimedBasedActionComparator(boolean onStart, boolean diffSamePeriods) {
        this.diffSimultaneous = diffSamePeriods;
        this.startBased = onStart;
    }

    @Override
    public int compare(Action a1, Action a2) {
        int d = startBased ? a1.getStart() - a2.getStart() : a1.getEnd() - a2.getEnd();
        if (d == 0) {
            if (a1.equals(a2)) {
                return 0;
            } else {
                d = startBased ? a1.getEnd() - a2.getEnd() : a1.getStart() - a2.getStart();
                //At this level we don't care but we must not return 0 because the action will
                //not be added
                if (diffSimultaneous && d == 0) {
                    return -1;
                }
                return d;
            }
        } else {
            return d;
        }
    }
}
