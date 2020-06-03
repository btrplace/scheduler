/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
            nextNodeId = Math.max(nextNodeId, id + 1);
            return new Node(id);
        }
        return null;
    }

    @Override
    public ElementBuilder copy() {
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
