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

package btrplace.model.view;

import java.util.*;

/**
 * An interface to denote a resource that nodes share among the VMs they host
 * <p/>
 * The interface allows to specify the physical resource capacity of the nodes
 * and the amount of virtual resources to allocate to the VMs.
 * By default, one unit of virtual resource corresponds to one unit of physical resource.
 * It is however possible to create a overbooking factor using
 * {@link btrplace.model.constraint.Overbook} constraints.
 *
 * @author Fabien Hermenier
 */
public class ShareableResource implements ModelView, Cloneable {

    /**
     * The base of the view identifier. Once instantiated, it is completed
     * by the resource identifier.
     */
    public static final String VIEW_ID_BASE = "ShareableResource.";

    private Map<Integer, Integer> vmsConsumption;
    private Map<Integer, Integer> nodesCapacity;

    private int vmsNoValue;
    private int nodesNoValue;

    private String viewId;

    private String rcId;

    public static final int DEFAULT_NO_VALUE = 0;

    /**
     * Make a new resource that use {@link #DEFAULT_NO_VALUE} as value to denote an undefined value.
     *
     * @param id the resource identifier
     */
    public ShareableResource(String id) {
        this(id, DEFAULT_NO_VALUE, DEFAULT_NO_VALUE);
    }

    /**
     * Make a new resource.
     *
     * @param id           the resource identifier
     * @param nodeDefValue the nodes default capacity
     * @param vmDefValue   the VM default consumption
     */
    public ShareableResource(String id, int nodeDefValue, int vmDefValue) {
        vmsConsumption = new HashMap<>();
        nodesCapacity = new HashMap<>();
        this.rcId = id;
        this.viewId = new StringBuilder(VIEW_ID_BASE).append(rcId).toString();
        this.nodesNoValue = nodeDefValue;
        this.vmsNoValue = vmDefValue;
    }

    /**
     * Get the VM consumption for that resource.
     *
     * @param vm the VM identifier
     * @return the capacity if it was defined or the default value.
     */
    public int getVMConsumption(int vm) {
        return get(vmsConsumption, vm, vmsNoValue);
    }

    private int get(Map<Integer, Integer> m, int id, int def) {
        if (m.containsKey(id)) {
            return m.get(id);
        }
        return def;
    }

    /**
     * Get the node capacity for that resource.
     *
     * @param n the node identifier
     * @return the capacity if it was defined or the default value.
     */
    public int getNodeCapacity(int n) {
        return get(nodesCapacity, n, nodesNoValue);
    }

    /**
     * Get the capacity for a list of nodes.
     *
     * @param ids the node identifiers
     * @return the capacity for each node. The order is maintained
     */
    public List<Integer> getNodesCapacity(List<Integer> ids) {
        return get(false, ids);
    }

    private List<Integer> get(boolean vm, List<Integer> ids) {
        List<Integer> res = new ArrayList<>(ids.size());
        for (int n : ids) {
            if (vm) {
                res.add(get(vmsConsumption, n, vmsNoValue));
            } else {
                res.add(get(nodesCapacity, n, nodesNoValue));
            }
        }
        return res;
    }

    /**
     * Get the consumption for a list of VMs.
     *
     * @param ids the VM identifiers
     * @return the consumption for each VM. The order is maintained
     */
    public List<Integer> getVMsConsumption(List<Integer> ids) {
        return get(true, ids);
    }


    /**
     * Get the VMs with defined consumptions.
     *
     * @return a set of VM identifiers. May be empty
     */
    public Set<Integer> getDefinedVMs() {
        return vmsConsumption.keySet();
    }

    /**
     * Get the nodes with defined capacities
     *
     * @return a set of node identifiers. May be empty
     */
    public Set<Integer> getDefinedNodes() {
        return nodesCapacity.keySet();
    }

    /**
     * Define the resource consumption of a VM.
     *
     * @param vm  the VM identifier
     * @param val the value to set
     * @return the current resource
     */
    public ShareableResource setVMConsumption(int vm, int val) {
        vmsConsumption.put(vm, val);
        return this;
    }

    /**
     * Define the resource consumption of a node.
     *
     * @param n   the node identifier
     * @param val the value to set
     * @return the current resource
     */
    public ShareableResource setNodeCapacity(int n, int val) {
        nodesCapacity.put(n, val);
        return this;
    }

    /**
     * Un-define a resource for a VM.
     *
     * @param n the VM identifier
     * @return {@code true} iff a value was previously defined for {@code n}.
     */
    public boolean unsetVM(int n) {
        return vmsConsumption.remove(n) != null;
    }

    /**
     * Un-define a resource for a node.
     *
     * @param n the node identifier
     * @return {@code true} iff a value was previously defined for {@code n}.
     */
    public boolean unsetNode(int n) {
        return nodesCapacity.remove(n) != null;
    }


    /**
     * Check if the resource consumption is defined for a VM.
     *
     * @param n the VM identifier
     * @return {@code true} iff the resource is defined for {@code n}.
     */
    public boolean consumptionDefined(int n) {
        return vmsConsumption.containsKey(n);
    }

    /**
     * Check if the resource capacity is defined for a node.
     *
     * @param n the node identifier
     * @return {@code true} iff the resource is defined for {@code n}.
     */
    public boolean capacityDefined(int n) {
        return nodesCapacity.containsKey(n);
    }


    /**
     * Get the view identifier.
     *
     * @return "ShareableResource.rcId" where rcId is the resource identifier provided to the constructor
     */
    @Override
    public String getIdentifier() {
        return viewId;
    }

    /**
     * Get the resource identifier
     *
     * @return a non-empty string
     */
    public String getResourceIdentifier() {
        return rcId;
    }

    /**
     * Get the maximum value for a set of VMs or nodes.
     *
     * @param ids   the identifiers
     * @param isVM  {@code true} to consider the VMs consumption. {@code false} for the nodes capacity
     * @param undef {@code true} to include the undefined elements using the default value
     * @return the value
     */
    public int max(Collection<Integer> ids, boolean isVM, boolean undef) {
        int m = Integer.MIN_VALUE;
        for (int u : ids) {
            if (!isVM && (capacityDefined(u) || undef)) {
                int x = capacityDefined(u) ? nodesCapacity.get(u) : nodesNoValue;
                if (x > m) {
                    m = x;
                }
            }
            if (isVM && (consumptionDefined(u) || undef)) {
                int x = consumptionDefined(u) ? vmsConsumption.get(u) : vmsNoValue;
                if (x > m) {
                    m = x;
                }
            }
        }
        return m;
    }

    /**
     * Get the minimal value for a set of VMs or nodes.
     *
     * @param ids   the identifiers
     * @param isVM  {@code true} to consider the VMs consumption. {@code false} for the nodes capacity
     * @param undef {@code true} to include the undefined elements using the default value
     * @return the value
     */
    public int min(Collection<Integer> ids, boolean isVM, boolean undef) {
        int m = Integer.MAX_VALUE;
        for (int u : ids) {
            if (!isVM && (capacityDefined(u) || undef)) {
                int x = capacityDefined(u) ? nodesCapacity.get(u) : nodesNoValue;
                if (x < m) {
                    m = x;
                }
            }
            if (isVM && (consumptionDefined(u) || undef)) {
                int x = consumptionDefined(u) ? vmsConsumption.get(u) : vmsNoValue;
                if (x < m) {
                    m = x;
                }
            }

        }
        return m;
    }

    /**
     * Sum VM consumptions or node capacities.
     *
     * @param ids   the identifiers.
     * @param isVM  {@code true} to sum VMs consumption. {@code false} to sum nodes capacity
     * @param undef {@code true} to include the undefined elements using the default value
     * @return the value
     */
    public int sum(Collection<Integer> ids, boolean isVM, boolean undef) {
        int s = 0;
        for (int u : ids) {
            if (isVM && (consumptionDefined(u) || undef)) {
                s += consumptionDefined(u) ? vmsConsumption.get(u) : vmsNoValue;
            }
            if (!isVM && (capacityDefined(u) || undef)) {
                s += capacityDefined(u) ? nodesCapacity.get(u) : nodesNoValue;
            }

        }
        return s;
    }

    /**
     * Get the default VM consumption.
     *
     * @return the value.
     */
    public int getDefaultConsumption() {
        return vmsNoValue;
    }

    /**
     * Get the default node capacity.
     *
     * @return the value.
     */
    public int getDefaultCapacity() {
        return nodesNoValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ShareableResource that = (ShareableResource) o;

        if (!that.getDefinedVMs().equals(vmsConsumption.keySet())) {
            return false;
        }

        if (!that.getDefinedNodes().equals(nodesCapacity.keySet())) {
            return false;
        }

        for (int k : vmsConsumption.keySet()) {
            if (!vmsConsumption.get(k).equals(that.getVMConsumption(k))) {
                return false;
            }
        }

        for (int k : nodesCapacity.keySet()) {
            if (!nodesCapacity.get(k).equals(that.getNodeCapacity(k))) {
                return false;
            }
        }
        return rcId.equals(that.getResourceIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(rcId, vmsConsumption, vmsNoValue, nodesCapacity, nodesNoValue);
    }

    @Override
    public ShareableResource clone() {
        ShareableResource rc = new ShareableResource(rcId, nodesNoValue, vmsNoValue);
        for (Map.Entry<Integer, Integer> e : vmsConsumption.entrySet()) {
            rc.vmsConsumption.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<Integer, Integer> e : nodesCapacity.entrySet()) {
            rc.nodesCapacity.put(e.getKey(), e.getValue());
        }
        return rc;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("rc:").append(rcId).append(':');
        for (Iterator<Map.Entry<Integer, Integer>> ite = nodesCapacity.entrySet().iterator(); ite.hasNext(); ) {
            Map.Entry<Integer, Integer> e = ite.next();
            buf.append("<node ").append(e.getKey().toString()).append(',').append(e.getValue()).append('>');
            if (ite.hasNext()) {
                buf.append(',');
            }
        }
        for (Iterator<Map.Entry<Integer, Integer>> ite = vmsConsumption.entrySet().iterator(); ite.hasNext(); ) {
            Map.Entry<Integer, Integer> e = ite.next();
            buf.append("<VM ").append(e.getKey().toString()).append(',').append(e.getValue()).append('>');
            if (ite.hasNext()) {
                buf.append(',');
            }
        }
        return buf.toString();
    }

    @Override
    public boolean substituteVM(int oldint, int newint) {
        setVMConsumption(newint, getVMConsumption(oldint));
        return true;
    }
}
