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

package org.btrplace.model.view;

import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.*;

/**
 * A view to denote a resource that nodes share among the VMs they host
 * <p>
 * The view allows to specify the physical resource capacity of the nodes
 * and the amount of virtual resources allocated to the VMs.
 * <p>
 * Associated constraints:
 * <ul>
 * <li>{@link org.btrplace.model.constraint.Preserve} to ensure the availability of a certain amount of <b>virtual resources</b> for a VM</li>
 * <li>{@link org.btrplace.model.constraint.ResourceCapacity} to cap the amount of <b>physical resources</b> that can be used on a node</li>
 * <li>{@link org.btrplace.model.constraint.Overbook} to specify a mapping between the virtual and the physical resources.</li>
 * </ul>
 * <p>
 * By default, if there is no {@link org.btrplace.model.constraint.Preserve} constraint for a VM, it is considered the VM requires
 * the same amount of virtual resources it is currently consuming.
 * <p>
 * By default, if there is no {@link org.btrplace.model.constraint.Overbook} constraint for a node, a conservative ratio
 * of <b>1</b> is used. This means one unit of virtual resources consumes one unit of physical resources.
 *
 * @author Fabien Hermenier
 */
public class ShareableResource implements ModelView, Cloneable {

    /**
     * The base of the view identifier. Once instantiated, it is completed
     * by the resource identifier.
     */
    public static final String VIEW_ID_BASE = "ShareableResource.";

    private Map<VM, Integer> vmsConsumption;
    private Map<Node, Integer> nodesCapacity;

    private int vmsNoValue;
    private int nodesNoValue;

    private String viewId;

    private String rcId;

    public static final int DEFAULT_NO_VALUE = 0;

    /**
     * Make a new resource that use {@link #DEFAULT_NO_VALUE}
     * for both VMs and nodes.
     *
     * @param r the resource identifier
     */
    public ShareableResource(String r) {
        this(r, DEFAULT_NO_VALUE, DEFAULT_NO_VALUE);
    }

    /**
     * Make a new resource.
     *
     * @param id             the resource identifier
     * @param defCapacity    the nodes default capacity
     * @param defConsumption the VM default consumption
     */
    public ShareableResource(String id, int defCapacity, int defConsumption) {
        vmsConsumption = new HashMap<>();
        nodesCapacity = new HashMap<>();
        this.rcId = id;
        this.viewId = VIEW_ID_BASE + rcId;
        this.nodesNoValue = defCapacity;
        this.vmsNoValue = defConsumption;
    }

    /**
     * Get the VM consumption.
     *
     * @param vm the VM
     * @return its consumption if it was defined otherwise the default value.
     */
    public int getConsumption(VM vm) {
        if (vmsConsumption.containsKey(vm)) {
            return vmsConsumption.get(vm);
        }
        return vmsNoValue;
    }

    /**
     * Get the node capacity.
     *
     * @param n the node
     * @return its capacity if it was defined otherwise the default value.
     */
    public int getCapacity(Node n) {
        if (nodesCapacity.containsKey(n)) {
            return nodesCapacity.get(n);
        }
        return nodesNoValue;

    }

    /**
     * Get the capacity for a list of nodes.
     *
     * @param ids the node identifiers
     * @return the capacity of each node. The order is maintained
     */
    public List<Integer> getCapacities(List<Node> ids) {
        List<Integer> res = new ArrayList<>(ids.size());
        for (Node n : ids) {
            res.add(getCapacity(n));
        }
        return res;
    }

    /**
     * Get the consumption for a list of VMs.
     *
     * @param ids the VM identifiers
     * @return the consumption of each VM. The order is maintained
     */
    public List<Integer> getConsumptions(List<VM> ids) {
        List<Integer> res = new ArrayList<>(ids.size());
        for (VM vm : ids) {
            res.add(getConsumption(vm));
        }
        return res;
    }

    /**
     * Get the VMs with defined consumptions.
     *
     * @return a set that may be empty
     */
    public Set<VM> getDefinedVMs() {
        return vmsConsumption.keySet();
    }

    /**
     * Get the nodes with defined capacities
     *
     * @return a set that may be empty
     */
    public Set<Node> getDefinedNodes() {
        return nodesCapacity.keySet();
    }

    /**
     * Set the resource consumption of a VM.
     *
     * @param vm  the VM
     * @param val the value to set
     * @return the current resource
     */
    public ShareableResource setConsumption(VM vm, int val) {
        vmsConsumption.put(vm, val);
        return this;
    }

    /**
     * Set the resource consumption of a node.
     *
     * @param n   the node
     * @param val the value to set
     * @return the current resource
     */
    public ShareableResource setCapacity(Node n, int val) {
        nodesCapacity.put(n, val);
        return this;
    }

    /**
     * Unset a VM consumption.
     *
     * @param vm the VM
     * @return {@code true} iff a value was previously defined for {@code n}.
     */
    public boolean unset(VM vm) {
        return vmsConsumption.remove(vm) != null;
    }

    /**
     * Unset a node capacity.
     *
     * @param n the node
     * @return {@code true} iff a value was previously defined for {@code n}.
     */
    public boolean unset(Node n) {
        return nodesCapacity.remove(n) != null;
    }


    /**
     * Check if the resource consumption is defined for a VM.
     *
     * @param vm the VM
     * @return {@code true} iff the consumption is defined.
     */
    public boolean consumptionDefined(VM vm) {
        return vmsConsumption.containsKey(vm);
    }

    /**
     * Check if the resource capacity is defined for a node.
     *
     * @param n the node identifier
     * @return {@code true} iff the capacity is defined}.
     */
    public boolean capacityDefined(Node n) {
        return nodesCapacity.containsKey(n);
    }

    /**
     * Get the view identifier.
     *
     * @return {@code "ShareableResource.rcId"} where rcId equals {@link #getResourceIdentifier()}
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

        if (!this.vmsConsumption.equals(that.vmsConsumption) ||
                !this.nodesCapacity.equals(that.nodesCapacity)) {
            return false;
        }
        return rcId.equals(that.getResourceIdentifier()) && getDefaultCapacity() == that.getDefaultCapacity()
                && getDefaultConsumption() == that.getDefaultConsumption();
    }

    @Override
    public int hashCode() {
        return Objects.hash(rcId, vmsConsumption, vmsNoValue, nodesCapacity, nodesNoValue);
    }

    @Override
    public ShareableResource clone() {
        ShareableResource rc = new ShareableResource(rcId, nodesNoValue, vmsNoValue);
        for (Map.Entry<VM, Integer> e : vmsConsumption.entrySet()) {
            rc.vmsConsumption.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<Node, Integer> e : nodesCapacity.entrySet()) {
            rc.nodesCapacity.put(e.getKey(), e.getValue());
        }
        return rc;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("rc:").append(rcId).append(':');
        for (Iterator<Map.Entry<Node, Integer>> ite = nodesCapacity.entrySet().iterator(); ite.hasNext(); ) {
            Map.Entry<Node, Integer> e = ite.next();
            buf.append("<node ").append(e.getKey().toString()).append(',').append(e.getValue()).append('>');
            if (ite.hasNext()) {
                buf.append(',');
            }
        }
        for (Iterator<Map.Entry<VM, Integer>> ite = vmsConsumption.entrySet().iterator(); ite.hasNext(); ) {
            Map.Entry<VM, Integer> e = ite.next();
            buf.append("<VM ").append(e.getKey().toString()).append(',').append(e.getValue()).append('>');
            if (ite.hasNext()) {
                buf.append(',');
            }
        }
        return buf.toString();
    }

    @Override
    public boolean substituteVM(VM oldRef, VM newRef) {
        setConsumption(newRef, getConsumption(oldRef));
        return true;
    }

    /**
     * Get the cumulative VMs consumption.
     *
     * @param ids   the VMs.
     * @param undef {@code true} to include the undefined elements using the default value
     * @return the value
     */
    public int sumConsumptions(Collection<VM> ids, boolean undef) {
        int s = 0;
        for (VM u : ids) {
            if (consumptionDefined(u) || undef) {
                s += consumptionDefined(u) ? vmsConsumption.get(u) : vmsNoValue;
            }
        }
        return s;
    }

    /**
     * Get the cumulative nodes capacity.
     *
     * @param ids   the nodes.
     * @param undef {@code true} to include the undefined elements using the default value
     * @return the value
     */
    public int sumCapacities(Collection<Node> ids, boolean undef) {
        int s = 0;
        for (Node u : ids) {
            if (capacityDefined(u) || undef) {
                s += capacityDefined(u) ? nodesCapacity.get(u) : nodesNoValue;
            }
        }
        return s;
    }
}
