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
    private Set<Integer>[] nodeState;

    /**
     * The VMs by state (running, sleeping, ready).
     */
    private Set<Integer>[] vmState;

    /**
     * The current location of the VMs.
     */
    private Map<Integer, Integer> place;

    /**
     * The VMs hosted by each node, by state (running or sleeping)
     */
    private Map<Integer, Set<Integer>>[] host;

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
        for (int off : m.getOfflineNodes()) {
            addOfflineNode(off);
        }
        for (int r : m.getReadyVMs()) {
            addReadyVM(r);
        }
        for (int on : m.getOnlineNodes()) {
            addOnlineNode(on);
            for (int r : m.getRunningVMs(on)) {
                addRunningVM(r, on);
            }
            for (int s : m.getSleepingVMs(on)) {
                addSleepingVM(s, on);
            }

        }
    }

    @Override
    public boolean addRunningVM(int vm, int nId) {
        if (!nodeState[ONLINE_STATE].contains(nId)) {
            return false;
        }

        if (vmState[RUNNING_STATE].contains(vm)) {
            //If was running, get it's old position
            int old = place.put(vm, nId);
            if (old != nId) {
                host[RUNNING_STATE].get(old).remove(vm);
                host[RUNNING_STATE].get(nId).add(vm);
            }
        } else if (vmState[SLEEPING_STATE].remove(vm)) {
            //If was sleeping, where ?
            vmState[RUNNING_STATE].add(vm);
            int old = place.put(vm, nId);
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
    public boolean addSleepingVM(int vm, int nId) {
        if (!nodeState[ONLINE_STATE].contains(nId)) {
            return false;
        }
        if (vmState[RUNNING_STATE].remove(vm)) {
            //If was running, sync the state
            vmState[SLEEPING_STATE].add(vm);
            int old = place.put(vm, nId);
            host[RUNNING_STATE].get(old).remove(vm);
            host[SLEEPING_STATE].get(nId).add(vm);
        } else if (vmState[SLEEPING_STATE].contains(vm)) {
            //If was sleeping, sync the state
            int old = place.put(vm, nId);
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
    public void addReadyVM(int vm) {
        if (vmState[RUNNING_STATE].remove(vm)) {
            //If was running, sync the state
            vmState[READY_STATE].add(vm);
            int n = place.remove(vm);
            host[RUNNING_STATE].get(n).remove(vm);
        } else if (vmState[SLEEPING_STATE].remove(vm)) {
            //If was sleeping, sync the state
            vmState[READY_STATE].add(vm);
            int n = place.remove(vm);
            host[SLEEPING_STATE].get(n).remove(vm);
        } else {
            //else, it's a new VM
            vmState[READY_STATE].add(vm);
        }
    }

    @Override
    public boolean removeVM(int vm) {
        if (place.containsKey(vm)) {
            int n = this.place.remove(vm);
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
    public boolean removeNode(int n) {
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
    public void addOnlineNode(int n) {
        nodeState[OFFLINE_STATE].remove(n);
        nodeState[ONLINE_STATE].add(n);
        host[RUNNING_STATE].put(n, new HashSet<Integer>());
        host[SLEEPING_STATE].put(n, new HashSet<Integer>());
    }

    @Override
    public boolean addOfflineNode(int n) {

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
    public Set<Integer> getOnlineNodes() {
        return nodeState[ONLINE_STATE];
    }

    @Override
    public Set<Integer> getOfflineNodes() {
        return nodeState[OFFLINE_STATE];
    }

    @Override
    public Set<Integer> getRunningVMs() {
        return vmState[RUNNING_STATE];
    }

    @Override
    public Set<Integer> getSleepingVMs() {
        return vmState[SLEEPING_STATE];
    }

    @Override
    public Set<Integer> getSleepingVMs(int n) {
        Set<Integer> in = host[SLEEPING_STATE].get(n);
        if (in == null) {
            return new HashSet<>();
        }
        return in;
    }

    @Override
    public Set<Integer> getRunningVMs(int n) {
        Set<Integer> in = host[RUNNING_STATE].get(n);
        if (in == null) {
            return new HashSet<>();
        }
        return in;
    }

    @Override
    public Set<Integer> getReadyVMs() {
        return vmState[READY_STATE];
    }

    @Override
    public Set<Integer> getAllVMs() {
        Set<Integer> vms = new HashSet<>(
                vmState[READY_STATE].size() +
                        vmState[SLEEPING_STATE].size() +
                        vmState[RUNNING_STATE].size());
        vms.addAll(vmState[READY_STATE]);
        vms.addAll(vmState[SLEEPING_STATE]);
        vms.addAll(vmState[RUNNING_STATE]);
        return vms;
    }

    @Override
    public Set<Integer> getAllNodes() {
        Set<Integer> ns = new HashSet<>(
                nodeState[OFFLINE_STATE].size() +
                        nodeState[ONLINE_STATE].size());
        ns.addAll(nodeState[OFFLINE_STATE]);
        ns.addAll(nodeState[ONLINE_STATE]);
        return ns;
    }

    @Override
    public int getVMLocation(int vm) {
        if (!place.containsKey(vm)) {
            return -1;
        }
        return place.get(vm);
    }

    @Override
    public Set<Integer> getRunningVMs(Collection<Integer> ns) {
        Set<Integer> vms = new HashSet<>();
        for (int n : ns) {
            vms.addAll(getRunningVMs(n));
        }
        return vms;
    }

    @Override
    public Mapping clone() {
        Mapping c2 = new DefaultMapping();
        for (int n : getOnlineNodes()) {
            c2.addOnlineNode(n);
            for (int v : getRunningVMs(n)) {
                c2.addRunningVM(v, n);
            }
            for (int v : getSleepingVMs(n)) {
                c2.addSleepingVM(v, n);
            }
        }
        for (int v : getReadyVMs()) {
            c2.addReadyVM(v);
        }
        for (int n : getOfflineNodes()) {
            c2.addOfflineNode(n);
        }
        return c2;
    }

    @Override
    public boolean containsNode(int n) {
        return nodeState[OFFLINE_STATE].contains(n) || nodeState[ONLINE_STATE].contains(n);
    }

    @Override
    public boolean containsVM(int vm) {
        return vmState[READY_STATE].contains(vm) || vmState[RUNNING_STATE].contains(vm) || vmState[SLEEPING_STATE].contains(vm);
    }

    @Override
    public void clear() {
        for (Set<Integer> st : nodeState) {
            st.clear();
        }
        for (Set<Integer> st : vmState) {
            st.clear();
        }
        place.clear();
        for (Map<Integer, Set<Integer>> h : host) {
            h.clear();
        }
    }

    @Override
    public void clearNode(int u) {
        //Get the VMs on the node
        for (Map<Integer, Set<Integer>> h : host) {
            Set<Integer> s = h.get(u);
            if (s != null) {
                for (int vm : s) {
                    place.remove(vm);
                    for (Set<Integer> st : vmState) {
                        st.remove(vm);
                    }
                }
                s.clear();
            }
        }
    }

    @Override
    public void clearAllVMs() {
        for (Set<Integer> st : vmState) {
            st.clear();
        }
        place.clear();
        for (Map<Integer, Set<Integer>> h : host) {
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

        for (int n : getOnlineNodes()) {
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
        for (int n : getOnlineNodes()) {
            result += n * (Objects.hash(getRunningVMs(n), getSleepingVMs(n)));
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        for (int n : nodeState[ONLINE_STATE]) {
            buf.append(n);
            buf.append(':');
            if (this.getRunningVMs(n).isEmpty() && this.getSleepingVMs(n).isEmpty()) {
                buf.append(" - ");
            }
            for (int vm : this.getRunningVMs(n)) {
                buf.append(' ').append(vm);
            }
            for (int vm : this.getSleepingVMs(n)) {
                buf.append(" (").append(vm).append(')');
            }
            buf.append('\n');
        }

        for (int n : nodeState[OFFLINE_STATE]) {
            buf.append('(').append(n).append(")\n");
        }

        buf.append("READY");

        for (int vm : this.getReadyVMs()) {
            buf.append(' ').append(vm);
        }

        return buf.append('\n').toString();
    }
}
