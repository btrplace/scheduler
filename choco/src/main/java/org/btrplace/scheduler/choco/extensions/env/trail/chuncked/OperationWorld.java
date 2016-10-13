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

import org.chocosolver.memory.structure.Operation;

/**
 * @author Fabien Hermenier
 */
public class OperationWorld implements World {


    /**
     * Stack of values (former values that need be restored upon backtracking).
     */
    private Operation[] valueStack;

    private int now;

    /**
     * Make a new world.
     *
     * @param defaultSize the default world size
     */
    public OperationWorld(int defaultSize) {
        now = 0;
        valueStack = new Operation[defaultSize];
    }

    /**
     * Reacts when a Operation is done: push the former value on the stacks
     */
    public void savePreviousState(Operation oldValue) {
        valueStack[now] = oldValue;
        now++;
        if (now == valueStack.length) {
            resizeUpdateCapacity();
        }
    }

    @Override
    public void revert() {
        for (int i = now - 1; i >= 0; i--) {
            Operation o = valueStack[i];
            o.undo();
        }
    }

    private void resizeUpdateCapacity() {
        final int newCapacity = (valueStack.length * 3) / 2;
        // First, copy the stack of former values
        final Operation[] tmp2 = new Operation[newCapacity];
        System.arraycopy(valueStack, 0, tmp2, 0, valueStack.length);
        valueStack = tmp2;
    }

    @Override
    public int used() {
        return now;
    }

    @Override
    public void clear() {
        now = 0;
    }

    @Override
    public int allocated() {
        return valueStack == null ? 0 : valueStack.length;
    }
}
