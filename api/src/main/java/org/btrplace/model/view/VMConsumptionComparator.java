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

package org.btrplace.model.view;

import org.btrplace.model.VM;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A comparator to compare VMs consumption wrt. multiple resources.
 *
 * @author Fabien Hermenier
 */
public class VMConsumptionComparator implements Comparator<VM>, Serializable {

    /**
     * The resources to use to make the comparison.
     */
    private List<ShareableResource> rcs;

    /**
     * The ordering criteria for each resource.
     */
    private List<Integer> ascs;

    /**
     * Make a new comparator.
     * Comparison will be in ascending order
     *
     * @param rc the resource to consider.
     */
    public VMConsumptionComparator(ShareableResource rc) {
        this(rc, true);
    }

    /**
     * Make a new comparator.
     *
     * @param rc  the resource to consider
     * @param asc {@code true} for an ascending comparison
     */
    public VMConsumptionComparator(ShareableResource rc, boolean asc) {
        this.rcs = new ArrayList<>();
        this.ascs = new ArrayList<>();

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
    public VMConsumptionComparator append(ShareableResource r, boolean asc) {
        rcs.add(r);
        ascs.add(asc ? 1 : -1);
        return this;
    }

    @Override
    public int compare(VM v1, VM v2) {
        for (int i = 0; i < rcs.size(); i++) {
            ShareableResource rc = rcs.get(i);
            int ret = rc.getConsumption(v1) - rc.getConsumption(v2);
            if (ret != 0) {
                return ascs.get(i) * ret;
            }
        }
        return 0;
    }
}
