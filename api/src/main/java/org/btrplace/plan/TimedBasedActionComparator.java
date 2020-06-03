/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.plan.event.Action;

import java.util.Comparator;

/**
 * A comparator to sort the actions in the increasing order of their starting moment.
 * If several actions starts at the same moment, the action that ends first in considered.
 * <p>
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
        if (a1.equals(a2)) {
            return 0;
        }
        int d = delay(a1, a2, startBased);
        if (d == 0) {
            //Compare wrt. the other bound
            d = delay(a1, a2, !startBased);
            if (diffSimultaneous && d == 0) {
                //At this level we don't care but we must not return 0 because the action will
                //not be added
                return -1;
            }
        }
        return d;
    }

    private static int delay(Action a1, Action a2, boolean onStart) {
        if (onStart) {
            return a1.getStart() - a2.getStart();
        }
        return a1.getEnd() - a2.getEnd();
    }
}
