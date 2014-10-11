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
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.set.hash.THashSet;

import java.util.*;

/**
 * Default implementation of {@link Mapping}.
 * <p>
 * Methods {@link #getRunningVMs()}, {@link #getSleepingVMs()}, {@link #getAllVMs()}, {@link #getAllNodes()},
 * {@link #getRunningVMs(Collection)}, {@link #getSleepingVMs(java.util.Collection)} have a O(n) complexity.
 * <p>
 * Methods {@code is*()} have a O(1) complexity.
 *
 * @author Fabien Hermenier
 */
public class DefaultMapping implements Mapping, Cloneable {

    private static final int RUNNING_STATE = 0;

    private static final int SLEEPING_STATE = 1;

    private static final int READY_STATE = 2;

    private static final int ONLINE_STATE = 0;

    private static final int OFFLINE_STATE = 1;

    /**
     * The node by states (online, offline)
     */
    private Set<Node>[] nodeState;

    /**
     * The state of each VM.
     */
    private TIntIntHashMap st;

    /**
     * The current location of the VMs.
     */
    private TIntObjectHashMap<Node> place;

    /**
     * The VMs that are in the ready state.
     */
    private Set<VM> vmReady;

    /**
     * The VMs hosted by each node, by state (running or sleeping)
     */
    private TIntObjectHashMap<Set<VM>>[] host;

    /**
     * Create a new mapping.
     */
    @SuppressWarnings("unchecked")
    public DefaultMapping() {

        nodeState = new Set[2];
        nodeState[ONLINE_STATE] = new THashSet<>();
        nodeState[OFFLINE_STATE] = new THashSet<>();

        vmReady = new THashSet<>();

        place = new TIntObjectHashMap<>();

        host = new TIntObjectHashMap[2];
        host[RUNNING_STATE] = new TIntObjectHashMap<>();
        host[SLEEPING_STATE] = new TIntObjectHashMap<>();

        st = new TIntIntHashMap(100, 0.5f, -1, -1);
    }

    @Override
    public boolean isRunning(VM v) {
        return st.get(v.id()) == RUNNING_STATE;
    }

    @Override
    public boolean isSleeping(VM v) {
        return st.get(v.id()) == SLEEPING_STATE;
    }

    @Override
    public boolean isReady(VM v) {
        return st.get(v.id()) == READY_STATE;
    }

    @Override
    public boolean isOnline(Node n) {
        return nodeState[ONLINE_STATE].contains(n);
    }

    @Override
    public boolean isOffline(Node n) {
        return nodeState[OFFLINE_STATE].contains(n);
    }

    /**
     * Make a new mapping from an existing one.
     *
     * @param m the mapping to copy
     */
    public DefaultMapping(Mapping m) {
        this();
        MappingUtils.fill(m, this);
    }

    @Override
    public boolean addRunningVM(VM vm, Node n) {
        if (!nodeState[ONLINE_STATE].contains(n)) {
            return false;
        }
        Node old;

        int vmId = vm.id();
        int nId = n.id();
        Set<VM> on = host[RUNNING_STATE].get(nId);
        if (on == null) {
            on = new THashSet<>();
            host[RUNNING_STATE].put(nId, on);
        }
        switch (st.get(vmId)) {
            case RUNNING_STATE:
                old = place.put(vmId, n);
                if (!old.equals(n)) {
                    host[RUNNING_STATE].get(old.id()).remove(vm);
                    on.add(vm);
                }
                break;
            case SLEEPING_STATE:
                old = place.put(vmId, n);
                host[SLEEPING_STATE].get(old.id()).remove(vm);
                on.add(vm);
                st.put(vmId, RUNNING_STATE);
                break;
            case READY_STATE:
                place.put(vmId, n);
                on.add(vm);
                vmReady.remove(vm);
                st.put(vmId, RUNNING_STATE);
                break;
            default:
                place.put(vmId, n);
                on.add(vm);
                st.put(vmId, RUNNING_STATE);
        }
        return true;
    }

    @Override
    public boolean addSleepingVM(VM vm, Node n) {
        if (!nodeState[ONLINE_STATE].contains(n)) {
            return false;
        }
        int nId = n.id();
        int vmId = vm.id();
        Set<VM> on = host[SLEEPING_STATE].get(nId);
        if (on == null) {
            on = new THashSet<>();
            host[SLEEPING_STATE].put(nId, on);
        }
        Node old;
        switch (st.get(vmId)) {
            case RUNNING_STATE:
                //If was running, sync the state
                old = place.put(vmId, n);
                host[RUNNING_STATE].get(old.id()).remove(vm);
                on.add(vm);
                st.put(vmId, SLEEPING_STATE);
                break;
            case SLEEPING_STATE:
                //If was sleeping, sync the state
                old = place.put(vmId, n);
                if (!old.equals(n)) {
                    host[SLEEPING_STATE].get(old.id()).remove(vm);
                    on.add(vm);
                }
                break;
            case READY_STATE:
                place.put(vmId, n);
                on.add(vm);
                vmReady.remove(vm);
                st.put(vmId, SLEEPING_STATE);
                break;
            default:
                //it's a new VM
                place.put(vmId, n);
                host[SLEEPING_STATE].get(nId).add(vm);
                st.put(vmId, SLEEPING_STATE);
        }
        st.put(vm.id(), SLEEPING_STATE);
        return true;
    }

    @Override
    public boolean addReadyVM(VM vm) {

        Node n = place.remove(vm.id());
        switch (st.get(vm.id())) {
            case RUNNING_STATE:
                //If was running, sync the state
                host[RUNNING_STATE].get(n.id()).remove(vm);
                break;
            case SLEEPING_STATE:
                //If was sleeping, sync the state
                host[SLEEPING_STATE].get(n.id()).remove(vm);
                break;
        }

        st.put(vm.id(), READY_STATE);
        vmReady.add(vm);
        return true;
    }

    @Override
    public boolean remove(VM vm) {
        if (place.containsKey(vm.id())) {
            Node n = this.place.remove(vm.id());
            //The VM exists and is already placed
            if (st.get(vm.id()) == RUNNING_STATE) {
                host[RUNNING_STATE].get(n.id()).remove(vm);
            } else if (st.get(vm.id()) == SLEEPING_STATE) {
                host[SLEEPING_STATE].get(n.id()).remove(vm);
            }
            st.remove(vm.id());
            return true;
        } else if (st.get(vm.id()) == READY_STATE) {

            vmReady.remove(vm);
            st.remove(vm.id());
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Node n) {
        if (nodeState[ONLINE_STATE].contains(n)) {
            int nId = n.id();
            Set<VM> on = host[RUNNING_STATE].get(nId);
            if (on != null) {
                if (!on.isEmpty()) {
                    return false;
                }
                host[RUNNING_STATE].remove(nId);
            }

            on = host[SLEEPING_STATE].get(nId);
            if (on != null) {
                if (!on.isEmpty()) {
                    return false;
                }
                host[SLEEPING_STATE].remove(nId);
            }
            return nodeState[ONLINE_STATE].remove(n);
        }

        return nodeState[OFFLINE_STATE].remove(n);
    }

    @Override
    public boolean addOnlineNode(Node n) {
        nodeState[OFFLINE_STATE].remove(n);
        nodeState[ONLINE_STATE].add(n);
        return true;
    }

    @Override
    public boolean addOfflineNode(Node n) {
        int nId = n.id();
        if (nodeState[ONLINE_STATE].contains(n)) {
            Set<VM> on = host[SLEEPING_STATE].get(nId);
            if (on != null && !on.isEmpty()) {
                return false;
            }
            on = host[RUNNING_STATE].get(nId);
            if (on != null && !on.isEmpty()) {
                return false;
            }
            nodeState[ONLINE_STATE].remove(n);
        }
        nodeState[OFFLINE_STATE].add(n);
        return true;
    }

    @Override
    public Set<Node> getOnlineNodes() {
        return nodeState[ONLINE_STATE];
    }

    @Override
    public Set<Node> getOfflineNodes() {
        return nodeState[OFFLINE_STATE];
    }

    @Override
    public Set<VM> getRunningVMs() {
        return getRunningVMs(getOnlineNodes());
    }

    @Override
    public Set<VM> getSleepingVMs() {
        return getSleepingVMs(getOnlineNodes());
    }

    @Override
    public Set<VM> getSleepingVMs(Node n) {
        Set<VM> in = host[SLEEPING_STATE].get(n.id());
        if (in == null) {
            return Collections.emptySet();
        }
        return in;
    }

    @Override
    public Set<VM> getRunningVMs(Node n) {
        Set<VM> in = host[RUNNING_STATE].get(n.id());
        if (in == null) {
            return Collections.emptySet();
        }
        return in;
    }

    @Override
    public Set<VM> getReadyVMs() {
        return vmReady;
    }

    @Override
    public Set<VM> getAllVMs() {
        final Set<VM> s = new HashSet<>(vmReady);
        host[RUNNING_STATE].forEachEntry(new TIntObjectProcedure<Set<VM>>() {
            @Override
            public boolean execute(int a, Set<VM> b) {
                s.addAll(b);
                return true;
            }
        });
        host[SLEEPING_STATE].forEachEntry(new TIntObjectProcedure<Set<VM>>() {
            @Override
            public boolean execute(int a, Set<VM> b) {
                s.addAll(b);
                return true;
            }
        });
        return s;
    }

    @Override
    public Set<Node> getAllNodes() {
        Set<Node> ns = new THashSet<>(
                nodeState[OFFLINE_STATE].size() +
                        nodeState[ONLINE_STATE].size()
        );
        ns.addAll(nodeState[OFFLINE_STATE]);
        ns.addAll(nodeState[ONLINE_STATE]);
        return ns;
    }

    @Override
    public Node getVMLocation(VM vm) {
        return place.get(vm.id());
    }

    @Override
    public Set<VM> getRunningVMs(Collection<Node> ns) {
        Set<VM> vms = new THashSet<>();
        for (Node n : ns) {
            vms.addAll(getRunningVMs(n));
        }
        return vms;
    }

    @Override
    public Set<VM> getSleepingVMs(Collection<Node> ns) {
        Set<VM> vms = new THashSet<>();
        for (Node n : ns) {
            vms.addAll(getSleepingVMs(n));
        }
        return vms;
    }

    @Override
    public Mapping clone() {
        return new DefaultMapping(this);
    }

    @Override
    public boolean contains(Node n) {
        return nodeState[OFFLINE_STATE].contains(n) || nodeState[ONLINE_STATE].contains(n);
    }

    @Override
    public boolean contains(VM vm) {
        return st.get(vm.id()) >= 0;
    }

    @Override
    public void clear() {
        for (Set<Node> s : nodeState) {
            s.clear();
        }
        st.clear();
        vmReady.clear();
        place.clear();
        for (TIntObjectHashMap<Set<VM>> h : host) {
            h.clear();
        }
    }

    @Override
    public void clearNode(Node u) {
        //Get the VMs on the node
        for (TIntObjectHashMap<Set<VM>> h : host) {
            Set<VM> s = h.get(u.id());
            if (s != null) {
                for (VM vm : s) {
                    place.remove(vm.id());
                    st.remove(vm.id());
                }
                s.clear();
            }
        }
    }

    @Override
    public void clearAllVMs() {
        place.clear();
        st.clear();
        vmReady.clear();
        for (TIntObjectHashMap<Set<VM>> h : host) {
            h.clear();
        }
    }

    @Override
    public boolean equals(Object o) {
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
    public String toString() {
        StringBuilder buf = new StringBuilder();

        for (Node n : nodeState[ONLINE_STATE]) {
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
        }

        for (Node n : nodeState[OFFLINE_STATE]) {
            buf.append('(').append(n).append(")\n");
        }

        buf.append("READY");

        for (VM vm : this.getReadyVMs()) {
            buf.append(' ').append(vm);
        }

        return buf.append('\n').toString();
    }

    @Override
    public int getNbNodes() {
        return nodeState[ONLINE_STATE].size() + nodeState[OFFLINE_STATE].size();
    }

    @Override
    public int getNbVMs() {
        return st.size();
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
