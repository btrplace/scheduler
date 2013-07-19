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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe implementation of {@link ElementBuilder}.
 *
 * @author Fabien Hermenier
 */
public class SynchronizedElementBuilder implements ElementBuilder {

    private AtomicInteger nextVMId;

    private AtomicInteger nextNodeId;

    private Set<VM> usedVMs;

    private Set<Node> usedNodes;

    /**
     * Make a new builder.
     *
     * @param usedVMs   VMs that are already registered
     * @param usedNodes Nodes that are already registered
     */
    public SynchronizedElementBuilder(Collection<VM> usedVMs, Collection<Node> usedNodes) {
        int maxVMId = 0;
        int maxNodeId = 0;
        this.usedVMs = Collections.synchronizedSet(new THashSet<VM>(usedVMs.size()));
        this.usedNodes = Collections.synchronizedSet(new THashSet<Node>(usedNodes.size()));
        for (VM v : usedVMs) {
            int i = v.id();
            if (i > maxVMId) {
                maxVMId = i + 1;
            }
            this.usedVMs.add(v);
        }
        for (Node n : usedNodes) {
            int i = n.id();
            if (i > maxNodeId) {
                maxNodeId = i + 1;
            }
            this.usedNodes.add(n);
        }
        nextVMId = new AtomicInteger(maxVMId);
        nextNodeId = new AtomicInteger(maxNodeId);
    }

    /**
     * Make a new builder.
     */
    public SynchronizedElementBuilder() {
        this(Collections.<VM>emptySet(), Collections.<Node>emptySet());
    }

    @Override
    public Node newNode() {
        Node n;
        do {
            int i = nextNodeId.getAndIncrement();
            n = i < 0 ? null : new Node(i);
        } while (n == null || !usedNodes.add(n));
        return n;
    }

    @Override
    public VM newVM() {
        VM v;
        do {
            int i = nextVMId.getAndIncrement();
            v = i < 0 ? null : new VM(i);
        } while (v == null || !usedVMs.add(v));
        return v;
    }

    @Override
    public VM newVM(int id) {
        VM v = new VM(id);
        if (!usedVMs.add(v)) {
            return null;
        }
        return v;
    }

    @Override
    public Node newNode(int id) {
        Node n = new Node(id);
        if (!usedNodes.add(n)) {
            return null;
        }
        return n;
    }

    @Override
    public Set<Node> getNodes() {
        return usedNodes;
    }

    @Override
    public Set<VM> getVMs() {
        return usedVMs;
    }

    @Override
    public ElementBuilder clone() {
        SynchronizedElementBuilder c = new SynchronizedElementBuilder();
        c.nextNodeId.set(nextNodeId.get());
        c.nextVMId.set(nextVMId.get());
        c.usedNodes.addAll(usedNodes);
        c.usedVMs.addAll(usedVMs);
        return c;
    }
}
