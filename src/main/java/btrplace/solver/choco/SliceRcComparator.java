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

import btrplace.model.StackableResource;

import java.util.Comparator;

/**
 * Compare slices with regards to the resource consumption of their associated element.
 *
 * @author Fabien Hermenier
 */
public class SliceRcComparator implements Comparator<Slice> {

    private StackableResource rc;

    private int ratio;

    /**
     * Make a new comparator.
     *
     * @param r   the resource used to perform the comparison
     * @param asc {@code true} for an ascending comparison
     */
    public SliceRcComparator(StackableResource r, boolean asc) {
        this.rc = r;
        ratio = asc ? 1 : -1;
    }

    @Override
    public int compare(Slice s1, Slice s2) {
        int x = rc.get(s1.getSubject());
        int y = rc.get(s2.getSubject());
        return ratio * (x - y);
    }
}
