/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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

import org.btrplace.model.PhysicalElement;

import java.util.BitSet;

/**
 * Default implementation of {@link LinkBuilder}.
 * Use a BitSet to generate identifiers and avoid duplication.
 * 
 * @author Vincent Kherbache
 */
public class DefaultLinkBuilder implements LinkBuilder {

    private BitSet usedIds;
    private int nextId;

    /**
     * New Builder.
     */
    public DefaultLinkBuilder() {
        usedIds = new BitSet();
    }

    @Override
    public Link newLink(int id, int capacity, Switch sw, PhysicalElement pe) {
        if (!usedIds.get(id)) {
            usedIds.set(id);
            nextId = Math.max(nextId, id + 1);
            return new Link(id, capacity, sw, pe);
        }
        return null;
    }

    @Override
    public Link newLink(int capacity, Switch sw, PhysicalElement pe) {
        int id = nextId++;
        if (id < 0) {
            //We look for holes in the bitset
            id = usedIds.nextClearBit(0);
        }
        usedIds.set(id);
        return new Link(id, capacity, sw, pe);
    }

    @Override
    public Link newLink(Switch sw, PhysicalElement pe) {
        return newLink(-1, sw, pe); // Infinite bandwidth (testing purpose)
    }

    @Override
    public boolean contains(Link l) {
        return usedIds.get(l.id());
    }

    @Override
    public LinkBuilder copy() {
        DefaultLinkBuilder sb = new DefaultLinkBuilder();
        sb.nextId = nextId;
        sb.usedIds = (BitSet) usedIds.clone();
        return sb;
    }
}
