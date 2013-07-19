/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.model;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

/**
 * Default implementation of {@link ElementBuilder}.
 * For a thread-safe implementation, see {@link SynchronizedElementBuilder}
 *
 * @author Fabien Hermenier
 */
public class DefaultElementBuilder implements ElementBuilder {

    private int nextVM;

    private int nextNode;

    private Set<VM> usedVMIds;
    private Set<Node> usedNodeIds;

    /**
     * New builder.
     */
    public DefaultElementBuilder() {
        usedNodeIds = new THashSet<>();
        usedVMIds = new THashSet<>();
        nextVM = 0;
        nextNode = 0;
    }

    @Override
    public VM newVM() {
        if (usedVMIds.size() == Integer.MAX_VALUE) {
            //No more ids left
            return null;
        }
        //Find the first free id.
        VM v = new VM(nextVM++);
        while (!usedVMIds.add(v)) {
            v = new VM(nextVM++);

        }
        return v;
    }

    @Override
    public Node newNode() {
        if (usedNodeIds.size() == Integer.MAX_VALUE) {
            //No more ids left
            return null;
        }

        //Find the first free id.
        Node n = new Node(nextNode++);
        while (!usedNodeIds.add(n)) {
            n = new Node(nextNode++);
        }
        return n;
    }

    @Override
    public VM newVM(int id) {
        VM v = new VM(id);
        if (!usedVMIds.add(v)) {
            return null;
        }
        return v;
    }

    @Override
    public Node newNode(int id) {
        Node n = new Node(id);
        if (!usedNodeIds.add(n)) {
            return null;
        }
        return n;
    }

    @Override
    public Set<Node> getNodes() {
        return usedNodeIds;
    }

    @Override
    public Set<VM> getVMs() {
        return usedVMIds;
    }

    @Override
    public ElementBuilder clone() {
        DefaultElementBuilder c = new DefaultElementBuilder();
        c.nextNode = nextNode;
        c.nextVM = nextVM;
        c.usedNodeIds = new THashSet<>(usedNodeIds);
        c.usedVMIds = new THashSet<>(usedVMIds);
        return c;
    }
}
