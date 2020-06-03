/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco;

import org.btrplace.model.VM;
import org.chocosolver.solver.variables.IntVar;


/**
 * Model a period where an element is hosted on a node.
 * {@link SliceBuilder} may be used to ease the creation of Slices.
 *
 * @author Fabien Hermenier
 */
public class Slice {

  private final IntVar hoster;

  private final IntVar start;

  private final IntVar end;

  private final IntVar duration;

  private final VM subject;

    /**
     * Make a new slice.
     *
     * @param s   the VM associated to the slice
     * @param st  the moment the slice starts
     * @param ed  the moment the slice ends
     * @param dur the slice duration
     * @param h   the slice host
     */
    public Slice(VM s, IntVar st, IntVar ed, IntVar dur, IntVar h) {

        this.start = st;
        this.end = ed;
        this.subject = s;
        this.hoster = h;
        this.duration = dur;
    }

    @Override
    public String toString() {
        return subject + "{from=" + printValue(getStart()) +
                ", to=" + printValue(getEnd()) +
                ", on=" + printValue(getHoster()) + '}';
    }

    private static String printValue(IntVar v) {
        if (v.isInstantiated()) {
            return Integer.toString(v.getValue());
        }
        return "[" + v.getLB() + ':' + v.getUB() + ']';
    }

    /**
     * Get the moment the slice starts.
     *
     * @return a variable denoting the moment
     */
    public IntVar getStart() {
        return start;
    }

    /**
     * Get the moment the slice ends.
     *
     * @return a variable denoting the moment
     */
    public IntVar getEnd() {
        return end;
    }

    /**
     * Get the duration of the slice.
     *
     * @return a variable denoting the moment
     */
    public IntVar getDuration() {
        return duration;
    }

    /**
     * Get the slice hoster.
     *
     * @return a variable indicating the node index
     */
    public IntVar getHoster() {
        return hoster;
    }

    /**
     * Get the VM associated to the slice.
     *
     * @return the VM identifier
     */
    public VM getSubject() {
        return subject;
    }
}
