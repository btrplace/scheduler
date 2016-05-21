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
package org.btrplace.scheduler.choco.extensions.env.trail;

import org.btrplace.scheduler.choco.extensions.env.MyEnvironmentTrailing;
import org.btrplace.scheduler.choco.extensions.env.StoredIntVector;


/**
 * Implements a trail with the history of all the stored search vectors.
 */
public class StoredIntVectorTrail implements TraceableStorage {

    /**
     * The current environment.
     */

    private final MyEnvironmentTrailing environment;


    /**
     * All the stored search vectors.
     */

    private StoredIntVector[] vectorStack;


    /**
     * Indices of the previous values in the stored vectors.
     */

    private int[] indexStack;


    /**
     * Previous values of the stored vector elements.
     */

    private int[] valueStack;


    /**
     * World stamps associated to the previous values
     */

    private int[] stampStack;

    /**
     * The last world an search vector was modified in.
     */

    private int currentLevel;


    /**
     * Starts of levels in all the history arrays.
     */

    private int[] worldStartLevels;

    /**
     * capacity of the env stack (in terms of number of updates that can be stored)
     */
    private int maxUpdates = 0;


    /**
     * Constructs a trail for the specified environment with the
     * specified numbers of updates and worlds.
     */

    public StoredIntVectorTrail(MyEnvironmentTrailing env, int nUpdates, int nWorlds) {
        this.environment = env;
        this.currentLevel = 0;
        maxUpdates = nUpdates;
        this.vectorStack = new StoredIntVector[nUpdates];
        this.indexStack = new int[nUpdates];
        this.valueStack = new int[nUpdates];
        this.stampStack = new int[nUpdates];
        this.worldStartLevels = new int[nWorlds];
    }


    /**
     * Reacts on the modification of an element in a stored search vector.
     */

    public void savePreviousState(StoredIntVector vect, int index, int oldValue, int oldStamp) {
        this.vectorStack[currentLevel] = vect;
        this.indexStack[currentLevel] = index;
        this.stampStack[currentLevel] = oldStamp;
        this.valueStack[currentLevel] = oldValue;
        currentLevel++;
        if (currentLevel == maxUpdates) {
            resizeUpdateCapacity();
        }
    }

    private void resizeUpdateCapacity() {
        final int newCapacity = ((maxUpdates * 3) / 2);
        // first, copy the stack of variables
        final StoredIntVector[] tmp1 = new StoredIntVector[newCapacity];
        System.arraycopy(vectorStack, 0, tmp1, 0, vectorStack.length);
        vectorStack = tmp1;
        // then, copy the stack of former values
        final int[] tmp2 = new int[newCapacity];
        System.arraycopy(valueStack, 0, tmp2, 0, valueStack.length);
        valueStack = tmp2;
        // then, copy the stack of world stamps
        final int[] tmp3 = new int[newCapacity];
        System.arraycopy(stampStack, 0, tmp3, 0, stampStack.length);
        stampStack = tmp3;
        // then, copy the stack of indices
        final int[] tmp4 = new int[newCapacity];
        System.arraycopy(indexStack, 0, tmp4, 0, indexStack.length);
        indexStack = tmp4;

        // last update the capacity
        maxUpdates = newCapacity;
    }

    public void resizeWorldCapacity(int newWorldCapacity) {
        final int[] tmp = new int[newWorldCapacity];
        System.arraycopy(worldStartLevels, 0, tmp, 0, worldStartLevels.length);
        worldStartLevels = tmp;
    }

    /**
     * Moving up to the next world.
     *
     * @param worldIndex current world index
     */

    public void worldPush(int worldIndex) {
        this.worldStartLevels[worldIndex] = currentLevel;
        if (worldIndex == worldStartLevels.length - 1) {
            resizeWorldCapacity(worldStartLevels.length * 3 / 2);
        }

    }


    /**
     * Moving down to the previous world.
     *
     * @param worldIndex current world index
     */

    public void worldPop(int worldIndex) {
        final int wsl = worldStartLevels[worldIndex];
        while (currentLevel > wsl) {
            currentLevel--;
            StoredIntVector v = vectorStack[currentLevel];
            v._set(indexStack[currentLevel], valueStack[currentLevel], stampStack[currentLevel]);
        }
    }


    /**
     * Comits a world: merging it with the previous one.
     */

    public void worldCommit(int worldIndex) {
        // principle:
        //   currentLevel decreases to end of previous world
        //   updates of the committed world are scanned:
        //     if their stamp is the previous one (merged with the current one) -> remove the update (garbage collecting this position for the next update)
        //     otherwise update the worldStamp
        int startLevel = worldStartLevels[environment.getWorldIndex()];
        int prevWorld = environment.getWorldIndex() - 1;
        int writeIdx = startLevel;
        for (int level = startLevel; level < currentLevel; level++) {
            StoredIntVector var = vectorStack[level];
            int idx = indexStack[level];
            int val = valueStack[level];
            int stamp = stampStack[level];
            var.worldStamps[idx] = prevWorld;// update the stamp of the variable (current stamp refers to a world that no longer exists)
            if (stamp != prevWorld) {
                // shift the update if needed
                if (writeIdx != level) {
                    valueStack[writeIdx] = val;
                    indexStack[writeIdx] = idx;
                    vectorStack[writeIdx] = var;
                    stampStack[writeIdx] = stamp;
                }
                writeIdx++;
            }  //else:writeIdx is not incremented and the update will be discarded (since a good one is in prevWorld)
        }
        currentLevel = writeIdx;
    }


    /**
     * Returns the current size of the stack.
     */

    public int getSize() {
        return currentLevel;
    }

    @Override
    public int allocated() {
        return stampStack.length;
    }
}
