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

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * A sub-mapping that is a limited version of a parent mapping.
 * The scope is defined as a collection of nodes that <b>is supposed</b> to belong to the parent mapping.
 * The same for the ready VMs.
 * <p>
 * Modifications made on the mapping are automatically reported on the parent mapping.
 * However, it is not allowed to remove a node or a VM.
 *
 * @author Fabien Hermenier
 */
public class SubMapping implements Mapping {

    private Mapping parent;

    private Set<Node> scope;

    private Set<VM> ready;

    private Set<VM> all = null;

    /**
     * Make a new mapping.
     *
     * @param p        the parent mapping
     * @param sc       the nodes that limit the scope of the new mapping. These nodes must all belong to the original mapping
     * @param subReady the subset of ready VMs to include in this mapping. These VMs must all belong to the original mapping
     */
    public SubMapping(Mapping p, Collection<Node> sc, Set<VM> subReady) {
        this.parent = p;
        this.scope = new THashSet<>(sc);
        this.ready = subReady;
    }

    /**
     * Make a new mapping.
     *
     * @param p  the parent mapping
     * @param sc the nodes that limit the scope of the new mapping. These nodes must all belong to the original mapping
     */
    public SubMapping(Mapping p, Collection<Node> sc) {
        this(p, sc, Collections.<VM>emptySet());
    }

    @Override
    public boolean addRunningVM(VM vm, Node n) {
        return !containsElsewhere(vm) && scope.contains(n) && parent.addRunningVM(vm, n);
    }

    @Override
    public boolean addSleepingVM(VM vm, Node n) {
        return !containsElsewhere(vm) && scope.contains(n) && parent.addSleepingVM(vm, n);
    }

    @Override
    public boolean addReadyVM(VM vm) {
        if (!containsElsewhere(vm)) {
            ready.add(vm);
            parent.addReadyVM(vm);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(VM vm) {
        if (contains(vm)) {
            ready.remove(vm);
            return parent.remove(vm);
        }
        return false;
    }

    @Override
    public boolean remove(Node n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Node> getOnlineNodes() {
        return onlyMyNodes(parent.getOnlineNodes());
    }

    @Override
    public boolean addOnlineNode(Node n) {
        return !containsElsewhere(n) && parent.addOnlineNode(n);
    }

    @Override
    public boolean addOfflineNode(Node n) {
        return !containsElsewhere(n) && parent.addOfflineNode(n);
    }

    @Override
    public Set<Node> getOfflineNodes() {
        return onlyMyNodes(parent.getOfflineNodes());
    }

    private Set<Node> onlyMyNodes(Set<Node> ns) {
        Set<Node> my = new THashSet<>();
        for (Node n : ns) {
            if (scope.contains(n)) {
                my.add(n);
            }
        }
        return my;
    }

    @Override
    public Set<VM> getRunningVMs() {
        return parent.getRunningVMs(scope);
    }

    @Override
    public Set<VM> getSleepingVMs() {
        return parent.getSleepingVMs(scope);
    }

    @Override
    public Set<VM> getSleepingVMs(Node n) {
        if (scope.contains(n)) {
            return parent.getSleepingVMs(n);
        }
        return Collections.emptySet();
    }

    @Override
    public Set<VM> getRunningVMs(Node n) {
        if (scope.contains(n)) {
            return parent.getRunningVMs(n);
        }
        return Collections.emptySet();
    }

    @Override
    public Set<VM> getReadyVMs() {
        return ready;
    }

    @Override
    public Set<VM> getAllVMs() {
        if (all == null) {
            all = new THashSet<>();
            for (Node n : scope) {
                all.addAll(parent.getRunningVMs(n));
                all.addAll(parent.getSleepingVMs(n));
            }
            all.addAll(ready);
        }
        return all;
    }

    @Override
    public Set<Node> getAllNodes() {
        //We suppose here that every node in the scope belong to the mapping
        return scope;
    }

    @Override
    public Node getVMLocation(VM vm) {
        Node n = parent.getVMLocation(vm);
        if (scope.contains(n)) {
            return n;
        }
        return null;
    }

    @Override
    public Set<VM> getRunningVMs(Collection<Node> ns) {
        Set<VM> res = new THashSet<>();
        for (Node n : ns) {
            if (scope.contains(n)) {
                res.addAll(parent.getRunningVMs(n));
            }
        }
        return res;
    }

    /**
     * Clone this mapping using a {@link DefaultMapping}.
     *
     * @return a mutable clone
     */
    @Override
    public DefaultMapping clone() {
        DefaultMapping c = new DefaultMapping();
        //Keep only the nodes inside the scope
        for (Node n : scope) {
            if (parent.getOnlineNodes().contains(n)) {
                c.addOnlineNode(n);
                for (VM v : parent.getRunningVMs(n)) {
                    c.addRunningVM(v, n);
                }
                for (VM v : parent.getSleepingVMs(n)) {
                    c.addSleepingVM(v, n);
                }
            } else if (parent.getOfflineNodes().contains(n)) {
                c.addOfflineNode(n);
            }
        }
        for (VM v : ready) {
            c.addReadyVM(v);
        }
        return c;
    }

    @Override
    public boolean contains(VM vm) {
        return ready.contains(vm) || scope.contains(parent.getVMLocation(vm));
    }

    private boolean containsElsewhere(VM vm) {
        //In parent, not in my (node || ready) scope
        return parent.contains(vm) && !ready.contains(vm) && !scope.contains(parent.getVMLocation(vm));
    }

    private boolean containsElsewhere(Node n) {
        return parent.contains(n) && !contains(n);
    }

    @Override
    public boolean contains(Node n) {
        return scope.contains(n) && (parent.isOnline(n) || parent.isOffline(n));
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearNode(Node u) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearAllVMs() {
        throw new UnsupportedOperationException();
    }

    /**
     * Fill an index with the VM presents in this mapping
     *
     * @param index the index to fill
     * @param p     the index value to use for each VM in the mapping
     */
    public void fillVMIndex(TIntIntHashMap index, int p) {
        for (Node n : scope) {
            for (VM v : parent.getRunningVMs(n)) {
                index.put(v.id(), p);
            }
            for (VM v : parent.getSleepingVMs(n)) {
                index.put(v.id(), p);
            }
        }
        for (VM v : ready) {
            index.put(v.id(), p);
        }
    }

    @Override
    public boolean isRunning(VM v) {
        return scope.contains(parent.getVMLocation(v)) && parent.isRunning(v);
    }

    @Override
    public boolean isSleeping(VM v) {
        return scope.contains(parent.getVMLocation(v)) && parent.isSleeping(v);
    }

    @Override
    public boolean isReady(VM v) {
        return ready.contains(v);
    }

    @Override
    public boolean isOnline(Node n) {
        return scope.contains(n) && parent.isOnline(n);
    }

    @Override
    public boolean isOffline(Node n) {
        return scope.contains(n) && parent.isOffline(n);
    }

    @Override
    public Set<VM> getSleepingVMs(Collection<Node> ns) {
        Set<VM> res = new THashSet<>();
        for (Node n : ns) {
            if (scope.contains(n)) {
                res.addAll(parent.getSleepingVMs(n));
            }
        }
        return res;
    }

    /**
     * Get the parent mapping.
     *
     * @return a non-null mapping
     */
    public Mapping getParent() {
        return parent;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        for (Node n : scope) {
            if (parent.isOnline(n)) {
                buf.append(n);
                buf.append(':');
                if (this.getRunningVMs(n).isEmpty() && this.getSleepingVMs(n).isEmpty()) {
                    buf.append(" - ");
                }
                for (VM vm : this.getRunningVMs(n)) {
                    buf.append(' ').append(vm);
                }
                for (VM vm : this.getSleepingVMs(n)) {
                    buf.append(" (").append(vm).append(')');
                }
                buf.append('\n');
            } else if (parent.isOffline(n)) {
                buf.append('(').append(n).append(")\n");
            }
        }
        buf.append("READY");

        for (VM vm : ready) {
            buf.append(' ').append(vm);
        }

        return buf.append('\n').toString();
    }

    @Override
    public int getNbNodes() {
        return scope.size();
    }

    @Override
    public int getNbVMs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        //TODO: not very efficient
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mapping)) {
            return false;
        }

        Mapping that = (Mapping) o;

        if (!getOnlineNodes().equals(that.getOnlineNodes())
                || !getOfflineNodes().equals(that.getOfflineNodes())
                || !getReadyVMs().equals(that.getReadyVMs())) {
            return false;
        }

        for (Node n : getOnlineNodes()) {
            if (!getRunningVMs(n).equals(that.getRunningVMs(n))
                    || !getSleepingVMs(n).equals(that.getSleepingVMs(n))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getOfflineNodes(), getReadyVMs(), getOnlineNodes());
        for (Node n : getOnlineNodes()) {
            result += Objects.hash(n, getRunningVMs(n), getSleepingVMs(n));
        }
        return result;
    }

    @Override
    public VMState getState(VM v) {
        if (isRunning(v)) {
            return VMState.RUNNING;
        } else if (isSleeping(v)) {
            return VMState.SLEEPING;
        } else if (isReady(v)) {
            return VMState.READY;
        }
        return null;
    }

    @Override
    public NodeState getState(Node n) {
        if (isOnline(n)) {
            return NodeState.ONLINE;
        } else if (isOffline(n)) {
            return NodeState.OFFLINE;
        }
        return null;
    }
}
