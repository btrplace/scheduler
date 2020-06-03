/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
