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

import java.util.UUID;

/**
 * Model a period where an element is hosted on a node.
 * {@link SliceBuilder} may be used to ease the creation of Slices.
 *
 * @author Fabien Hermenier
 */
public class Slice {

    private IntDomainVar hoster;

    private IntDomainVar start;

    private IntDomainVar end;

    private IntDomainVar duration;

    private IntDomainVar excl;

    private UUID subject;

    /**
     * Make a new slice.
     *
     * @param s    the element associated to the slice
     * @param st   the moment the slice starts
     * @param ed   the moment the slice ends
     * @param dur  the slice duration
     * @param h    the slice host
     * @param excl the exclusive status of the slice
     */
    public Slice(UUID s, IntDomainVar st, IntDomainVar ed, IntDomainVar dur, IntDomainVar h, IntDomainVar excl) {

        this.start = st;
        this.end = ed;
        this.subject = s;
        this.hoster = h;
        this.excl = excl;
        this.duration = dur;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(subject).append("{from=");
        if (getStart().isInstantiated()) {
            b.append(getStart().getVal());
        } else {
            b.append('[').append(getStart().getInf()).append(':').append(getStart().getSup()).append(']');

        }
        b.append(" to=");
        if (getEnd().isInstantiated()) {
            b.append(getEnd().getVal());
        } else {
            b.append('[').append(getEnd().getInf()).append(':').append(getEnd().getSup()).append(']');

        }


        return b.append('}').toString();
    }

    /**
     * Get the moment the slice starts.
     *
     * @return a variable denoting the moment
     */
    public IntDomainVar getStart() {
        return start;
    }

    /**
     * Get the moment the slice ends.
     *
     * @return a variable denoting the moment
     */
    public IntDomainVar getEnd() {
        return end;
    }

    /**
     * Get the duration of the slice.
     *
     * @return a variable denoting the moment
     */
    public IntDomainVar getDuration() {
        return duration;
    }

    /**
     * Get the slice hoster.
     *
     * @return a variable indicating the node index
     */
    public IntDomainVar getHoster() {
        return hoster;
    }

    /**
     * Indicates whether or not the slice can overlap other ones.
     *
     * @return {@code 1} to indicate that no overlap is allowed
     */
    public IntDomainVar isExclusive() {
        return excl;
    }

    /**
     * Get the subject of the slice.
     *
     * @return the subject identifier
     */
    public UUID getSubject() {
        return subject;
    }
}
