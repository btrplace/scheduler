package org.btrplace.model.view.net;

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
    public LinkBuilder clone() {
        DefaultLinkBuilder sb = new DefaultLinkBuilder();
        sb.nextId = nextId;
        sb.usedIds = (BitSet) usedIds.clone();
        return sb;
    }
}
