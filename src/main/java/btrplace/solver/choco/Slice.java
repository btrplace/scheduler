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
import choco.kernel.solver.variables.scheduling.TaskVar;

/**
 * Created with IntelliJ IDEA.
 * User: fhermeni
 * Date: 15/11/12
 * Time: 13:13
 * To change this template use File | Settings | File Templates.
 */
public class Slice {

    private TaskVar task;

    private IntDomainVar hoster;

    private String name;

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
        return task.end();
    }

    public IntDomainVar getEnd() {
        return task.end();
    }

    public IntDomainVar getDuration() {
        return task.duration();
    }

    public IntDomainVar getHoster() {
        return hoster;
    }


}
