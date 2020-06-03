/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco;

import org.btrplace.model.view.ShareableResource;

import java.util.Comparator;

/**
 * Compare slices with regards to the resource consumption of their associated element.
 *
 * @author Fabien Hermenier
 */
public class SliceRcComparator implements Comparator<Slice> {

  private final ShareableResource rc;

  private final int ratio;

    /**
     * Make a new comparator.
     *
     * @param r   the resource used to perform the comparison
     * @param asc {@code true} for an ascending comparison
     */
    public SliceRcComparator(ShareableResource r, boolean asc) {
        this.rc = r;
        ratio = asc ? 1 : -1;
    }

    @Override
    public int compare(Slice s1, Slice s2) {
        int x = rc.getConsumption(s1.getSubject());
        int y = rc.getConsumption(s2.getSubject());
        return ratio * (x - y);
    }
}
