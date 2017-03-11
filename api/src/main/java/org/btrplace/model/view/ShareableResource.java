/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SideConstraint;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

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
@SideConstraint(args = {"id : string"}, inv = "!(n : nodes) sum({cons(v, id). v : running(n)}) <= capa(n, id)")
public class ShareableResource implements ModelView {

    /**
     * The base of the view identifier. Once instantiated, it is completed
     * by the resource identifier.
     */
    public static final String VIEW_ID_BASE = "ShareableResource.";

    private TObjectIntHashMap<VM> vmsConsumption;
    private TObjectIntHashMap<Node> nodesCapacity;

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
        this.rcId = id;
        vmsConsumption = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, defConsumption);
        nodesCapacity = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, defCapacity);
        if (defCapacity < 0) {
            throw new IllegalArgumentException(String.format("The %s default capacity must be >= 0", rcId));
        }
        if (defConsumption < 0) {
            throw new IllegalArgumentException(String.format("The %s default consumption must be >= 0", rcId));
        }


        this.viewId = VIEW_ID_BASE + rcId;
    }

    /**
     * Get the VM consumption.
     *
     * @param vm the VM
     * @return its consumption if it was defined otherwise the default value.
     */
    public int getConsumption(VM vm) {
        return vmsConsumption.get(vm);
    }

    /**
     * Get the node capacity.
     *
     * @param n the node
     * @return its capacity if it was defined otherwise the default value.
     */
    public int getCapacity(Node n) {
        return nodesCapacity.get(n);
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
        if (val < 0) {
            throw new IllegalArgumentException(String.format("The '%s' consumption of VM '%s' must be >= 0", rcId, vm));
        }
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
        if (val < 0) {
            throw new IllegalArgumentException(String.format("The '%s' capacity of node '%s' must be >= 0", rcId, n));
        }
        nodesCapacity.put(n, val);
        return this;
    }

    /**
     * Unset a VM consumption.
     *
     * @param vm the VM
     */
    public void unset(VM vm) {
        vmsConsumption.remove(vm);
    }

    /**
     * Unset a node capacity.
     *
     * @param n the node
     */
    public void unset(Node n) {
        nodesCapacity.remove(n);
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
   * Get the view identifier from a given resource identifier.
   *
   * @param rcId the resource identifier.
   * @return the resulting view.
   */
  public static String getIdentifier(String rcId) {
    return VIEW_ID_BASE + rcId;
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
        return vmsConsumption.getNoEntryValue();
    }

    /**
     * Get the default node capacity.
     *
     * @return the value.
     */
    public int getDefaultCapacity() {
        return nodesCapacity.getNoEntryValue();
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
        return Objects.hash(rcId, vmsConsumption, nodesCapacity);
    }

    @Override
    public ShareableResource copy() {
        ShareableResource rc = new ShareableResource(rcId, nodesCapacity.getNoEntryValue(), vmsConsumption.getNoEntryValue());
        vmsConsumption.forEachEntry((vm, c) -> {
            rc.vmsConsumption.put(vm, c);
            return true;
        });
        nodesCapacity.forEachEntry((n, c) -> {
            rc.nodesCapacity.put(n, c);
            return true;
        });
        return rc;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", String.format("rc:%s:", rcId), "");
        for (Node n : nodesCapacity.keySet()) {
            int c = nodesCapacity.get(n);
            joiner.add(String.format("<node %s,%d>", n, c));
        }

        StringJoiner vmJoiner = new StringJoiner(",");
        for (VM vm : vmsConsumption.keySet()) {
            int c = vmsConsumption.get(vm);
            vmJoiner.add(String.format("<VM %s,%d>", vm, c));
        }
        return String.format("%s%s", joiner, vmJoiner);
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
                s += vmsConsumption.get(u);
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
                s += nodesCapacity.get(u);
            }
        }
        return s;
    }

    /**
     * Get the view associated to a model if exists
     *
     * @param mo the model to look at
     * @return the view if attached. {@code null} otherwise
     */
    public static ShareableResource get(Model mo, String id) {
        return (ShareableResource) mo.getView(VIEW_ID_BASE + id);
    }
}
