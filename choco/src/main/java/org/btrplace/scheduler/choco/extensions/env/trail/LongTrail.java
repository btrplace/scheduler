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


import org.btrplace.scheduler.choco.extensions.env.StoredLong;
import org.chocosolver.memory.IStorage;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/04/13
 */
public interface LongTrail extends IStorage {

    void savePreviousState(StoredLong v, long oldValue, int oldStamp);

    void buildFakeHistory(StoredLong v, long initValue, int fromStamp);
}
