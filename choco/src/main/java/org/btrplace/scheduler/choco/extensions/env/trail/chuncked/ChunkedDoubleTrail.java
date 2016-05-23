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

import org.btrplace.scheduler.choco.extensions.env.StoredDouble;
import org.btrplace.scheduler.choco.extensions.env.trail.DoubleTrail;


/**
 * A trail for doubles.
 *
 * @author Fabien Hermenier
 */
public class ChunkedDoubleTrail extends ChunkedTrail<DoubleWorld> implements DoubleTrail {


    /**
     * Constructs a trail with predefined size.
     * @param size the default size
     */
    public ChunkedDoubleTrail(int size) {
        worlds = new DoubleWorld[size];
    }

    @Override
    public void worldPush(int worldIndex) {
        if (worlds[worldIndex] == null) {
            current = new DoubleWorld(preferredSize());
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
        DoubleWorld [] tmp = new DoubleWorld[newCapacity];
        System.arraycopy(worlds, 0, tmp, 0, worlds.length);
        worlds = tmp;
    }

    @Override
    public void savePreviousState(StoredDouble v, double oldValue, int oldStamp) {
        current.savePreviousState(v, oldValue, oldStamp);
    }

    @Override
    public void buildFakeHistory(StoredDouble v, double initValue, int olderStamp) {
        // first save the current state on the top of the stack
        savePreviousState(v, initValue, olderStamp - 1);
        //then rewrite other states
        for (int w = olderStamp; w > 1; w--) {
            DoubleWorld cur = worlds[olderStamp];
            cur.savePreviousState(v, initValue, w - 1);
        }
    }
}
