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

import org.btrplace.scheduler.choco.extensions.env.StoredBool;

/**
 * @author Fabien Hermenier
 */
public class BoolWorld implements World{

    /**
     * Stack of backtrackable search variables.
     */
    private StoredBool[] variableStack;

    /**
     * Stack of values (former values that need be restored upon backtracking).
     */
    private boolean[] valueStack;


    /**
     * Stack of timestamps indicating the world where the former value
     * had been written.
     */
    private int[] stampStack;

    private int now;

    public BoolWorld(int defaultSize) {
        now = 0;
        valueStack = new boolean[defaultSize];
        stampStack = new int[defaultSize];
        variableStack = new StoredBool[defaultSize];
    }

    /**
     * Reacts when a StoredBool is modified: push the former value & timestamp
     * on the stacks.
     */
    public void savePreviousState(StoredBool v, boolean oldValue, int oldStamp) {
        valueStack[now] = oldValue;
        variableStack[now] = v;
        stampStack[now] = oldStamp;
        now++;
        if (now == valueStack.length) {
            resizeUpdateCapacity();
        }
    }

    @Override
    public void revert() {
        for (int i = now - 1; i >= 0; i--) {
            StoredBool v = variableStack[i];
            v._set(valueStack[i], stampStack[i]);
        }
    }

    private void resizeUpdateCapacity() {
        final int newCapacity = ((variableStack.length * 3) / 2);
        final StoredBool[] tmp1 = new StoredBool[newCapacity];
        System.arraycopy(variableStack, 0, tmp1, 0, variableStack.length);
        variableStack = tmp1;
        final boolean[] tmp2 = new boolean[newCapacity];
        System.arraycopy(valueStack, 0, tmp2, 0, valueStack.length);
        valueStack = tmp2;
        final int[] tmp3 = new int[newCapacity];
        System.arraycopy(stampStack, 0, tmp3, 0, stampStack.length);
        stampStack = tmp3;
    }

    @Override
    public int used() {
        return now;
    }
}
