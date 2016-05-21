/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco.extensions.env.trail.chuncked;

import org.btrplace.scheduler.choco.extensions.env.trail.TraceableStorage;

/**
 * An abstract segmented trail.
 * @author Fabien Hermenier
 */
public abstract class ChunkedTrail<W extends World> implements TraceableStorage {

    /**
     * The worlds.
     */
    protected W[] worlds;

    /**
     * The current worl.
     */
    protected W current;

    /**
     * The default world size.
     */
    public static final int DEFAULT_SIZE = 512;

    /**
     * Moving down to the previous world.
     *
     * @param worldIndex current world index
     */
    public void worldPop(int worldIndex) {
        current.revert();
        current = null;
        if (worldIndex >= 2) {
            current = worlds[worldIndex - 1];
        }
    }

    public void worldCommit(int worldIndex) {
        throw new UnsupportedOperationException();
    }

    protected int preferredSize() {
        return DEFAULT_SIZE;
    }

    /**
     * Returns the allocated trail size.
     *
     * @return a positive number
     */
    public int allocated() {
        int n = 0;
        for (World w : worlds) {
            if (w != null) {
                n += w.allocated();
            }
        }
        return n;
    }

}
