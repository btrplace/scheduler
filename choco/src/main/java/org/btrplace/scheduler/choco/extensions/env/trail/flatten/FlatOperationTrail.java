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

package org.btrplace.scheduler.choco.extensions.env.trail.flatten;

import org.btrplace.scheduler.choco.extensions.env.trail.OperationTrail;
import org.btrplace.scheduler.choco.extensions.env.trail.TraceableStorage;
import org.chocosolver.memory.structure.Operation;

/**
 * @author Fabien Hermenier
 */
public class FlatOperationTrail implements OperationTrail, TraceableStorage {


    /**
     * Stack of values (former values that need be restored upon backtracking).
     */
    private Operation[] valueStack;


    /**
     * Points the level of the last entry.
     */
    private int currentLevel;


    /**
     * A stack of pointers (for each start of a world).
     */
    private int[] worldStartLevels;

    /**
     * Constructs a trail with predefined size.
     *
     * @param nUpdates maximal number of updates that will be stored
     * @param nWorlds  maximal number of worlds that will be stored
     */
    public FlatOperationTrail(int nUpdates, int nWorlds) {
        currentLevel = 0;
        valueStack = new Operation[nUpdates];
        worldStartLevels = new int[nWorlds];
    }


    /**
     * Moving up to the next world.
     *
     * @param worldIndex current world index
     */
    @Override
    public void worldPush(int worldIndex) {
        worldStartLevels[worldIndex] = currentLevel;
        if (worldIndex == worldStartLevels.length - 1) {
            resizeWorldCapacity(worldStartLevels.length * 3 / 2);
        }
    }


    /**
     * Moving down to the previous world.
     *
     * @param worldIndex current world index
     */
    @Override
    public void worldPop(int worldIndex) {
        final int wsl = worldStartLevels[worldIndex];
        while (currentLevel > wsl) {
            currentLevel--;
            valueStack[currentLevel].undo();
        }
    }


    /**
     * Returns the current size of the stack.
     */
    public int getSize() {
        return currentLevel;
    }


    /**
     * Commit a world: merging it with the previous one.
     */
    @Override
    public void worldCommit(int worldIndex) {
    }

    /**
     * Reacts when a StoredInt is modified: push the former value & timestamp
     * on the stacks.
     */
    @Override
    public void savePreviousState(Operation oldValue) {
        valueStack[currentLevel] = oldValue;
        currentLevel++;
        if (currentLevel == valueStack.length) {
            resizeUpdateCapacity();
        }
    }

    private void resizeUpdateCapacity() {
        final int newCapacity = (valueStack.length * 3) / 2;
        // First, copy the stack of former values
        final Operation[] tmp2 = new Operation[newCapacity];
        System.arraycopy(valueStack, 0, tmp2, 0, valueStack.length);
        valueStack = tmp2;
    }

    private void resizeWorldCapacity(int newWorldCapacity) {
        final int[] tmp = new int[newWorldCapacity];
        System.arraycopy(worldStartLevels, 0, tmp, 0, worldStartLevels.length);
        worldStartLevels = tmp;
    }

    /**
     * Returns the allocated trail size.
     *
     * @return a positive number
     */
    @Override
    public int allocated() {
        return valueStack.length;
    }
}
