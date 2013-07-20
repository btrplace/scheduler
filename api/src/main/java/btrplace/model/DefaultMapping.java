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

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.set.hash.THashSet;

import java.util.*;

/**
 * Default implementation of {@link Mapping}.
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

    private TIntIntHashMap st;

    /**
     * The current location of the VMs.
     */
    private TIntObjectHashMap<Node> place;

    private Set<VM> vmReady;
    /**
     * The VMs hosted by each node, by state (running or sleeping)
     */
    private TIntObjectHashMap<Set<VM>>[] host;

    /**
     * Create a new mapping.
     */
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

    /**
     * Make a new mapping from an existing one.
     *
     * @param m the mapping to copy
     */
    public DefaultMapping(Mapping m) {
        this();
        for (Node off : m.getOfflineNodes()) {
            addOfflineNode(off);
        }
        for (VM r : m.getReadyVMs()) {
            addReadyVM(r);
        }
        for (Node on : m.getOnlineNodes()) {
            addOnlineNode(on);
            for (VM r : m.getRunningVMs(on)) {
                addRunningVM(r, on);
            }
            for (VM s : m.getSleepingVMs(on)) {
                addSleepingVM(s, on);
            }
        }
    }

    @Override
    public boolean addRunningVM(VM vm, Node nId) {
        if (!nodeState[ONLINE_STATE].contains(nId)) {
            return false;
        }
        Node old;
        switch (st.get(vm.id())) {
            case RUNNING_STATE:
                old = place.put(vm.id(), nId);
                if (!old.equals(nId)) {
                    host[RUNNING_STATE].get(old.id()).remove(vm);
                    host[RUNNING_STATE].get(nId.id()).add(vm);
                }
                break;
            case SLEEPING_STATE:
                old = place.put(vm.id(), nId);
                host[SLEEPING_STATE].get(old.id()).remove(vm);
                host[RUNNING_STATE].get(nId.id()).add(vm);
                st.put(vm.id(), RUNNING_STATE);
                break;
            case READY_STATE:
                place.put(vm.id(), nId);
                host[RUNNING_STATE].get(nId.id()).add(vm);
                vmReady.remove(vm);
                st.put(vm.id(), RUNNING_STATE);
                break;
            default:
                place.put(vm.id(), nId);
                host[RUNNING_STATE].get(nId.id()).add(vm);
                st.put(vm.id(), RUNNING_STATE);
        }
        return true;
    }

    @Override
    public boolean addSleepingVM(VM vm, Node nId) {
        if (!nodeState[ONLINE_STATE].contains(nId)) {
            return false;
        }
        if (st.get(vm.id()) == RUNNING_STATE) {
            //If was running, sync the state
            Node old = place.put(vm.id(), nId);
            host[RUNNING_STATE].get(old.id()).remove(vm);
            host[SLEEPING_STATE].get(nId.id()).add(vm);
        }
        else if (st.get(vm.id()) == SLEEPING_STATE) {
            //If was sleeping, sync the state
            Node old = place.put(vm.id(), nId);
            if (!old.equals(nId)) {
                host[SLEEPING_STATE].get(old.id()).remove(vm);
                host[SLEEPING_STATE].get(nId.id()).add(vm);
            }
        }
        else if (st.get(vm.id()) == READY_STATE) {
            place.put(vm.id(), nId);
            host[SLEEPING_STATE].get(nId.id()).add(vm);
            vmReady.remove(vm);
        } else {
            //it's a new VM
            place.put(vm.id(), nId);
            host[SLEEPING_STATE].get(nId.id()).add(vm);
        }
        st.put(vm.id(), SLEEPING_STATE);
        return true;
    }

    @Override
    public void addReadyVM(VM vm) {

        if (st.get(vm.id()) == RUNNING_STATE) {
            //If was running, sync the state
            Node n = place.remove(vm.id());
            host[RUNNING_STATE].get(n.id()).remove(vm);
        }
        if (st.get(vm.id()) == SLEEPING_STATE) {
            //If was sleeping, sync the state
            Node n = place.remove(vm.id());
            host[SLEEPING_STATE].get(n.id()).remove(vm);
        }
        st.put(vm.id(), READY_STATE);
        vmReady.add(vm);
    }

    @Override
    public boolean remove(VM vm) {
        if (place.containsKey(vm.id())) {
            Node n = this.place.remove(vm.id());
            //The VM exists and is already placed
            if (st.get(vm.id()) == RUNNING_STATE) {
                host[RUNNING_STATE].get(n.id()).remove(vm);
            }
            else if (st.get(vm.id()) == SLEEPING_STATE) {
                host[SLEEPING_STATE].get(n.id()).remove(vm);
            }
            st.remove(vm.id());
            return true;
        } //else if (vmState[READY_STATE].remove(vm)) {
        else if (st.get(vm.id()) == READY_STATE) {
            vmReady.remove(vm);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Node n) {
        if (nodeState[ONLINE_STATE].contains(n)) {
            if (!host[RUNNING_STATE].get(n.id()).isEmpty() || !host[SLEEPING_STATE].get(n.id()).isEmpty()) {
                return false;
            }
            host[RUNNING_STATE].remove(n.id());
            host[SLEEPING_STATE].remove(n.id());
            return nodeState[ONLINE_STATE].remove(n);
        }
        return nodeState[OFFLINE_STATE].remove(n);
    }

    @Override
    public void addOnlineNode(Node n) {
        nodeState[OFFLINE_STATE].remove(n);
        nodeState[ONLINE_STATE].add(n);
        host[RUNNING_STATE].put(n.id(), new THashSet<VM>());
        host[SLEEPING_STATE].put(n.id(), new THashSet<VM>());
    }

    @Override
    public boolean addOfflineNode(Node n) {

        if (nodeState[ONLINE_STATE].contains(n)) {
            if (!host[RUNNING_STATE].get(n.id()).isEmpty() || !host[SLEEPING_STATE].get(n.id()).isEmpty()) {
                //It already host VMs, not possible
                return false;
            } else {
                nodeState[ONLINE_STATE].remove(n);
            }
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
                        nodeState[ONLINE_STATE].size());
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
        for (Set<Node> st : nodeState) {
            st.clear();
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
        /*for (Set<VM> st : vmState) {
            st.clear();
        } */
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
}
