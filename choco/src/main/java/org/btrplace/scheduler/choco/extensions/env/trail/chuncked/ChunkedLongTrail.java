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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.btrplace.scheduler.choco.extensions.env.StoredInt;
import org.btrplace.scheduler.choco.extensions.env.StoredLong;
import org.btrplace.scheduler.choco.extensions.env.trail.BoolTrail;
import org.btrplace.scheduler.choco.extensions.env.trail.LongTrail;


public class ChunkedLongTrail implements LongTrail {


    private LongWorld [] worlds;

    private LongWorld current;

    private int nbWorlds;

    /**
     * Constructs a trail with predefined size.
     */
    public ChunkedLongTrail(int size) {
        worlds = new LongWorld[size];
        nbWorlds = 0;
    }


    /**
     * Moving up to the next world.
     *
     * @param worldIndex current world index
     */

    public void worldPush(int worldIndex) {
        int size = 1024;
        if (current != null) {
            size = Math.max(current.used(), size);
        }
        current = new LongWorld(size);
        worlds[nbWorlds++] = current;
        if (nbWorlds == worlds.length) {
            resizeWorlds();
        }
    }

    private void resizeWorlds() {
        int newCapacity = ((worlds.length * 3) / 2);
        LongWorld [] tmp = new LongWorld[newCapacity];
        System.arraycopy(worlds, 0, tmp, 0, worlds.length);
        worlds = tmp;
    }


    /**
     * Moving down to the previous world.
     *
     * @param worldIndex current world index
     */

    public void worldPop(int worldIndex) {
        current.revert();
        nbWorlds--;
        current = null;
        if (nbWorlds >= 0) {
            current = worlds[nbWorlds];
        }
    }

    /**
     * Comits a world: merging it with the previous one.
     */

    public void worldCommit(int worldIndex) {
        throw new UnsupportedOperationException();
    }


    /**
     * Reacts when a StoredInt is modified: push the former value & timestamp
     * on the stacks.
     */
    public void savePreviousState(StoredLong v, long oldValue, int oldStamp) {
        current.savePreviousState(v, oldValue, oldStamp);
    }

    public void buildFakeHistory(StoredLong v, long initValue, int olderStamp) {
        throw new UnsupportedOperationException();
    }
}
