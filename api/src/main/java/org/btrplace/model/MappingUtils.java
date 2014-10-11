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
