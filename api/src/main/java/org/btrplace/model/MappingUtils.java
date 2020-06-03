/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

/**
 * Common tools to manipulate a {@link Mapping}.
 *
 * @author Fabien Hermenier
 */
public final class MappingUtils {

    /**
     * Utility class, no constructor.
     */
    private MappingUtils() {

    }

    /**
     * Fill a destination mapping with all the elements
     * in a source mapping
     *
     * @param src the mapping to copy
     * @param dst the destination mapping
     */
    public static void fill(Mapping src, Mapping dst) {
        for (Node off : src.getOfflineNodes()) {
            dst.addOfflineNode(off);
        }
        for (VM r : src.getReadyVMs()) {
            dst.addReadyVM(r);
        }
        for (Node on : src.getOnlineNodes()) {
            dst.addOnlineNode(on);
            for (VM r : src.getRunningVMs(on)) {
                dst.addRunningVM(r, on);
            }
            for (VM s : src.getSleepingVMs(on)) {
                dst.addSleepingVM(s, on);
            }

        }
    }
}
