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

package org.btrplace.model;

import java.util.BitSet;

/**
 * Default implementation of {@link ElementBuilder}.
 * For a thread-safe implementation, see {@link SynchronizedElementBuilder}
 *
 * @author Fabien Hermenier
 */
public class DefaultElementBuilder implements ElementBuilder {

    private BitSet usedVMIds;

    private BitSet usedNodeIds;

    private int nextNodeId;

    private int nextVMId;

    /**
     * New builder.
     */
    public DefaultElementBuilder() {
        usedNodeIds = new BitSet();
        usedVMIds = new BitSet();
    }

    @Override
    public VM newVM() {
        int id = nextVMId++;
        if (id < 0) {
            //We look for holes in the bitset
            id = usedVMIds.nextClearBit(0);
        }
        usedVMIds.set(id);
        return new VM(id);
    }

    @Override
    public Node newNode() {
        int id = nextNodeId++;
        if (id < 0) {
            //We look for holes in the bitset
            id = usedNodeIds.nextClearBit(0);
        }
        usedNodeIds.set(id);
        return new Node(id);
    }

    @Override
    public VM newVM(int id) {
        if (!usedVMIds.get(id)) {
            usedVMIds.set(id);
            nextVMId = Math.max(nextVMId, id + 1);
            return new VM(id);
        }
        return null;
    }

    @Override
    public Node newNode(int id) {
        if (!usedNodeIds.get(id)) {
            usedNodeIds.set(id);
            nextVMId = Math.max(nextVMId, id + 1);
            return new Node(id);
        }
        return null;
    }

    @Override
    public ElementBuilder clone() {
        DefaultElementBuilder c = new DefaultElementBuilder();
        c.nextVMId = nextVMId;
        c.nextNodeId = nextNodeId;
        c.usedVMIds = (BitSet) usedVMIds.clone();
        c.usedNodeIds = (BitSet) usedNodeIds.clone();
        return c;
    }

    @Override
    public boolean contains(VM v) {
        return usedVMIds.get(v.id());
    }

    @Override
    public boolean contains(Node n) {
        return usedNodeIds.get(n.id());
    }
}
