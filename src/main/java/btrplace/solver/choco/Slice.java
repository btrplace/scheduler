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

/**
 * Model a period where an element is hosted on a node.
 *
 * @author Fabien Hermenier
 */
public class Slice {

    private IntDomainVar hoster;

    private IntDomainVar start;

    private IntDomainVar end;

    private IntDomainVar duration;

    private String name;

    public Slice(String n, IntDomainVar st, IntDomainVar ed, IntDomainVar d, IntDomainVar h) {
        this.name = n;
        this.start = st;
        this.end = ed;
        this.duration = d;
        this.hoster = h;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(name).append("{from=");
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

    public IntDomainVar getStart() {
        return start;
    }

    public IntDomainVar getEnd() {
        return end;
    }

    public IntDomainVar getDuration() {
        return duration;
    }

    public IntDomainVar getHoster() {
        return hoster;
    }


}
