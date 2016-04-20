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

package org.btrplace.model.view.network;

import java.util.BitSet;

/**
 * Default implementation of {@link SwitchBuilder}.
 * Use a BitSet to generate identifiers and avoid duplication.
 *
 * @author Vincent Kherbache
 */
public class DefaultSwitchBuilder implements SwitchBuilder {

    private BitSet usedIds;
    private int nextId;

    /**
     * New Builder.
     */
    public DefaultSwitchBuilder() {
        usedIds = new BitSet();
    }

    @Override
    public Switch newSwitch(int id, int capacity) {
        if (!usedIds.get(id)) {
            usedIds.set(id);
            nextId = Math.max(nextId, id + 1);
            return new Switch(id, capacity);
        }
        return null;
    }

    @Override
    public Switch newSwitch(int capacity) {
        int id = nextId++;
        if (id < 0) {
            //We look for holes in the bitset
            id = usedIds.nextClearBit(0);
        }
        usedIds.set(id);
        return new Switch(id, capacity);
    }

    @Override
    public Switch newSwitch() {
        return newSwitch(-1);
    }

    @Override
    public boolean contains(Switch sw) {
        return usedIds.get(sw.id());
    }

    @Override
    public SwitchBuilder copy() {
        DefaultSwitchBuilder sb = new DefaultSwitchBuilder();
        sb.nextId = nextId;
        sb.usedIds = (BitSet) usedIds.clone();
        return sb;
    }
}
