/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.view;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.btrplace.model.VM;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A comparator to compare VMs consumption wrt. multiple resources.
 *
 * @author Fabien Hermenier
 */
public class VMConsumptionComparator implements Comparator<VM> {

  /**
   * The resources to use to make the comparison.
   */
  private final List<ShareableResource> rcs;

    /**
     * The ordering criteria for each resource.
     */
    private final TIntList ascs;

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
        this.ascs = new TIntArrayList();

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
