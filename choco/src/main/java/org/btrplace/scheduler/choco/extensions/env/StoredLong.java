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
package org.btrplace.scheduler.choco.extensions.env;

import org.btrplace.scheduler.choco.extensions.env.trail.LongTrail;
import org.chocosolver.memory.IStateLong;


/**
 * A class implementing backtrackable long.
 */
public class StoredLong extends IStateLong {

    protected final LongTrail myTrail;

    /**
     * Constructs a stored search with an initial value.
     * Note: this constructor should not be used directly: one should instead
     * use the IEnvironment factory
     */
    public StoredLong(final ChunkedTrailing env, final long i) {
        super(env, i);
        myTrail = env.getLongTrail();
        if(env.fakeHistoryNeeded()){
            myTrail.buildFakeHistory(this, i, timeStamp);
        }
    }

    /**
     * Modifies the value and stores if needed the former value on the
     * env stack.
     */
    @Override
    public final void set(final long y) {
        if (y != currentValue) {
            final int wi = environment.getWorldIndex();
            if (this.timeStamp < wi) {
                myTrail.savePreviousState(this, currentValue, timeStamp);
                timeStamp = wi;
            }
            currentValue = y;
        }
    }
}

