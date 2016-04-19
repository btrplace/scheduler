/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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

import org.btrplace.model.VM;
import org.chocosolver.solver.variables.IntVar;


/**
 * Model a period where an element is hosted on a node.
 * {@link SliceBuilder} may be used to ease the creation of Slices.
 * <p>
 * See {@link SliceUtils} to extract components of Slices.
 *
 * @author Fabien Hermenier
 * @see SliceUtils
 */
public class Slice {

    private IntVar hoster;

    private IntVar start;

    private IntVar end;

    private IntVar duration;

    private VM subject;

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

    private String printValue(IntVar v) {
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
