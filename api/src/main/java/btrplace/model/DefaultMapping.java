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

    /**
     * The VMs by state (running, sleeping, ready).
     */
    private Set<VM>[] vmState;

    /**
     * The current location of the VMs.
     */
    private Map<VM, Node> place;

    /**
     * The VMs hosted by each node, by state (running or sleeping)
     */
    private Map<Node, Set<VM>>[] host;

    /**
     * Create a new mapping.
     */
    public DefaultMapping() {
        nodeState = new Set[2];
        nodeState[ONLINE_STATE] = new HashSet<>();
        nodeState[OFFLINE_STATE] = new HashSet<>();

        vmState = new Set[3];
        vmState[RUNNING_STATE] = new HashSet<>();
        vmState[SLEEPING_STATE] = new HashSet<>();
        vmState[READY_STATE] = new HashSet<>();

        place = new HashMap<>();

        host = new Map[2];
        host[RUNNING_STATE] = new HashMap<>();
        host[SLEEPING_STATE] = new HashMap<>();
    }

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

        if (vmState[RUNNING_STATE].contains(vm)) {
            //If was running, get it's old position
            Node old = place.put(vm, nId);
            if (old != nId) {
                host[RUNNING_STATE].get(old).remove(vm);
                host[RUNNING_STATE].get(nId).add(vm);
            }
        } else if (vmState[SLEEPING_STATE].remove(vm)) {
            //If was sleeping, where ?
            vmState[RUNNING_STATE].add(vm);
            Node old = place.put(vm, nId);
            host[SLEEPING_STATE].get(old).remove(vm);
            host[RUNNING_STATE].get(nId).add(vm);
        } else if (vmState[READY_STATE].remove(vm)) {
            place.put(vm, nId);
            vmState[RUNNING_STATE].add(vm);
            host[RUNNING_STATE].get(nId).add(vm);
        } else {
            //it's a new VM
            place.put(vm, nId);
            vmState[RUNNING_STATE].add(vm);
            host[RUNNING_STATE].get(nId).add(vm);
        }
        return true;
    }

    @Override
    public boolean addSleepingVM(VM vm, Node nId) {
        if (!nodeState[ONLINE_STATE].contains(nId)) {
            return false;
        }
        if (vmState[RUNNING_STATE].remove(vm)) {
            //If was running, sync the state
            vmState[SLEEPING_STATE].add(vm);
            Node old = place.put(vm, nId);
            host[RUNNING_STATE].get(old).remove(vm);
            host[SLEEPING_STATE].get(nId).add(vm);
        } else if (vmState[SLEEPING_STATE].contains(vm)) {
            //If was sleeping, sync the state
            Node old = place.put(vm, nId);
            vmState[SLEEPING_STATE].add(vm);
            if (old != nId) {
                host[SLEEPING_STATE].get(old).remove(vm);
                host[SLEEPING_STATE].get(nId).add(vm);
            }
        } else if (vmState[READY_STATE].remove(vm)) {
            place.put(vm, nId);
            vmState[SLEEPING_STATE].add(vm);
            host[SLEEPING_STATE].get(nId).add(vm);
        } else {
            //it's a new VM
            place.put(vm, nId);
            vmState[SLEEPING_STATE].add(vm);
            host[SLEEPING_STATE].get(nId).add(vm);
        }
        return true;
    }

    @Override
    public void addReadyVM(VM vm) {
        if (vmState[RUNNING_STATE].remove(vm)) {
            //If was running, sync the state
            vmState[READY_STATE].add(vm);
            Node n = place.remove(vm);
            host[RUNNING_STATE].get(n).remove(vm);
        } else if (vmState[SLEEPING_STATE].remove(vm)) {
            //If was sleeping, sync the state
            vmState[READY_STATE].add(vm);
            Node n = place.remove(vm);
            host[SLEEPING_STATE].get(n).remove(vm);
        } else {
            //else, it's a new VM
            vmState[READY_STATE].add(vm);
        }
    }

    @Override
    public boolean remove(VM vm) {
        if (place.containsKey(vm)) {
            Node n = this.place.remove(vm);
            //The VM exists and is already placed
            if (vmState[RUNNING_STATE].remove(vm)) {
                host[RUNNING_STATE].get(n).remove(vm);
            } else if (vmState[SLEEPING_STATE].remove(vm)) {
                host[SLEEPING_STATE].get(n).remove(vm);
            }
            return true;
        } else if (vmState[READY_STATE].remove(vm)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Node n) {
        if (nodeState[ONLINE_STATE].contains(n)) {
            if (!host[RUNNING_STATE].get(n).isEmpty() || !host[SLEEPING_STATE].get(n).isEmpty()) {
                return false;
            }
            host[RUNNING_STATE].remove(n);
            host[SLEEPING_STATE].remove(n);
            return nodeState[ONLINE_STATE].remove(n);
        }
        return nodeState[OFFLINE_STATE].remove(n);
    }

    @Override
    public void addOnlineNode(Node n) {
        nodeState[OFFLINE_STATE].remove(n);
        nodeState[ONLINE_STATE].add(n);
        host[RUNNING_STATE].put(n, new HashSet<VM>());
        host[SLEEPING_STATE].put(n, new HashSet<VM>());
    }

    @Override
    public boolean addOfflineNode(Node n) {

        if (nodeState[ONLINE_STATE].contains(n)) {
            if (!host[RUNNING_STATE].get(n).isEmpty() || !host[SLEEPING_STATE].get(n).isEmpty()) {
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
        return vmState[RUNNING_STATE];
    }

    @Override
    public Set<VM> getSleepingVMs() {
        return vmState[SLEEPING_STATE];
    }

    @Override
    public Set<VM> getSleepingVMs(Node n) {
        Set<VM> in = host[SLEEPING_STATE].get(n);
        if (in == null) {
            return Collections.emptySet();
        }
        return in;
    }

    @Override
    public Set<VM> getRunningVMs(Node n) {
        Set<VM> in = host[RUNNING_STATE].get(n);
        if (in == null) {
            return Collections.emptySet();
        }
        return in;
    }

    @Override
    public Set<VM> getReadyVMs() {
        return vmState[READY_STATE];
    }

    @Override
    public Set<VM> getAllVMs() {
        Set<VM> vms = new HashSet<>(
                vmState[READY_STATE].size() +
                        vmState[SLEEPING_STATE].size() +
                        vmState[RUNNING_STATE].size());
        vms.addAll(vmState[READY_STATE]);
        vms.addAll(vmState[SLEEPING_STATE]);
        vms.addAll(vmState[RUNNING_STATE]);
        return vms;
    }

    @Override
    public Set<Node> getAllNodes() {
        Set<Node> ns = new HashSet<>(
                nodeState[OFFLINE_STATE].size() +
                        nodeState[ONLINE_STATE].size());
        ns.addAll(nodeState[OFFLINE_STATE]);
        ns.addAll(nodeState[ONLINE_STATE]);
        return ns;
    }

    @Override
    public Node getVMLocation(VM vm) {
        return place.get(vm);
    }

    @Override
    public Set<VM> getRunningVMs(Collection<Node> ns) {
        Set<VM> vms = new HashSet<>();
        for (Node n : ns) {
            vms.addAll(getRunningVMs(n));
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
        return vmState[READY_STATE].contains(vm) || vmState[RUNNING_STATE].contains(vm) || vmState[SLEEPING_STATE].contains(vm);
    }

    @Override
    public void clear() {
        for (Set<Node> st : nodeState) {
            st.clear();
        }
        for (Set<VM> st : vmState) {
            st.clear();
        }
        place.clear();
        for (Map<Node, Set<VM>> h : host) {
            h.clear();
        }
    }

    @Override
    public void clearNode(Node u) {
        //Get the VMs on the node
        for (Map<Node, Set<VM>> h : host) {
            Set<VM> s = h.get(u);
            if (s != null) {
                for (VM vm : s) {
                    place.remove(vm);
                    for (Set<VM> st : vmState) {
                        st.remove(vm);
                    }
                }
                s.clear();
            }
        }
    }

    @Override
    public void clearAllVMs() {
        for (Set<VM> st : vmState) {
            st.clear();
        }
        place.clear();
        for (Map<Node, Set<VM>> h : host) {
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
