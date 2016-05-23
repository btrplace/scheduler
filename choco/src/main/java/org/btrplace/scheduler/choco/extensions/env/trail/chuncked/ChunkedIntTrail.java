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

import org.btrplace.scheduler.choco.extensions.env.StoredInt;
import org.btrplace.scheduler.choco.extensions.env.trail.IntTrail;

/**
 * A trail for integers.
 *
 * @author Fabien Hermenier
 */

public class ChunkedIntTrail extends ChunkedTrail<IntWorld> implements IntTrail {

    private int ws;

    /**
     * Constructs a trail with predefined size.
     * @param ws the initial world size
     * @param nbWorlds the initial number of worlds
     */
    public ChunkedIntTrail(int ws, int nbWorlds) {
        worlds = new IntWorld[nbWorlds];
        this.ws = ws;
    }

    @Override
    public void worldPush(int worldIndex) {
        if (worlds[worldIndex] == null) {
            current = new IntWorld(ws);
            worlds[worldIndex] = current;
        } else {
            current = worlds[worldIndex];
            current.clear();
        }
        if (worldIndex == worlds.length - 1) {
            resizeWorlds();
        }
    }

    private void resizeWorlds() {
        int newCapacity = (worlds.length * 3) / 2;
        IntWorld [] tmp = new IntWorld[newCapacity];
        System.arraycopy(worlds, 0, tmp, 0, worlds.length);
        worlds = tmp;
    }

    @Override
    public void savePreviousState(StoredInt v, int oldValue, int oldStamp) {
        current.savePreviousState(v, oldValue, oldStamp);
    }

    @Override
    public void buildFakeHistory(StoredInt v, int initValue, int olderStamp) {
        // first save the current state on the top of the stack
        savePreviousState(v, initValue, olderStamp - 1);
        //then rewrite other states
        for (int w = olderStamp; w > 1; w--) {
            IntWorld cur = worlds[olderStamp];
            cur.savePreviousState(v, initValue, w - 1);
        }
    }
}
