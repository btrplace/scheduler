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


public class ChunkedIntTrail extends ChunkedTrail<IntWorld> implements IntTrail {

    /**
     * Constructs a trail with predefined size.
     */
    public ChunkedIntTrail(int size) {
        worlds = new IntWorld[size];
    }

    @Override
    public void worldPush(int worldIndex) {
        //if (worlds[worldIndex] == null) {
            current = new IntWorld(preferredSize());
            worlds[worldIndex] = current;
        /*} else {
            current = worlds[worldIndex];
            current.clear();
        }*/
        if (worldIndex == worlds.length - 1) {
            resizeWorlds();
        }
    }

    private void resizeWorlds() {
        int newCapacity = ((worlds.length * 3) / 2);
        IntWorld [] tmp = new IntWorld[newCapacity];
        System.arraycopy(worlds, 0, tmp, 0, worlds.length);
        worlds = tmp;
    }

    @Override
    public void savePreviousState(StoredInt v, int oldValue, int oldStamp) {
        current.savePreviousState(v, oldValue, oldStamp);
    }

    @Override
    public void buildFakeHistory(StoredInt v, int initValue, int fromStamp) {
        throw new UnsupportedOperationException();
    }
}
