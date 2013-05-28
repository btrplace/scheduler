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

package btrplace.model;

import btrplace.model.view.ShareableResource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A comparator to compare element depending on multiple resources.
 *
 * @author Fabien Hermenier
 */
public class ElementComparator implements Comparator<Integer> {

    /**
     * The resources to use to make the comparison.
     */
    private List<ShareableResource> rcs;

    /**
     * The ordering criteria for each resource.
     */
    private List<Integer> ascs;

    private boolean compareVM;

    /**
     * Make a new comparator.
     * Comparison will be in ascending order
     *
     * @param compareVM {@code true} to compare VMs. {@code false} for nodes
     * @param rc        the resource to consider.
     */
    public ElementComparator(boolean compareVM, ShareableResource rc) {
        this(compareVM, rc, true);
    }

    /**
     * Make a new comparator.
     *
     * @param rc  the resource to consider
     * @param asc {@code true} for an ascending comparison
     */
    public ElementComparator(boolean compareVM, ShareableResource rc, boolean asc) {
        this.rcs = new ArrayList<>();
        this.ascs = new ArrayList<>();
        this.compareVM = compareVM;

        rcs.add(rc);
        ascs.add(asc ? 1 : -1);
    }

    /**
     * Append a new resource to use to make the comparison
     *
     * @param r   the resource to add
     * @param asc {@code true} for an ascending comparison
     * @return the current comparator
     */
    public ElementComparator append(ShareableResource r, boolean asc) {
        rcs.add(r);
        ascs.add(asc ? 1 : -1);
        return this;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
        for (int i = 0; i < rcs.size(); i++) {
            ShareableResource rc = rcs.get(i);
            int ret;
            if (compareVM) {
                ret = rc.getVMConsumption(o1) - rc.getVMConsumption(o2);
            } else {
                ret = rc.getNodeCapacity(o1) - rc.getNodeCapacity(o2);
            }
            if (ret != 0) {
                return ascs.get(i) * ret;
            }
        }
        return 0;
    }
}
