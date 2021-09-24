/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.view;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SideConstraint;
import org.btrplace.util.IntMap;

import java.util.*;
import java.util.stream.Stream;

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
@SideConstraint(args = {"id : string"}, inv = "!(n : nodes) sum([cons(v, id). v : running(n)]) <= capa(n, id)")
public class ShareableResource implements ModelView {

  /**
   * The base of the view identifier. Once instantiated, it is completed
   * by the resource identifier.
   */
  public static final String VIEW_ID_BASE = "ShareableResource.";

  private IntMap vmsConsumption;
  private IntMap nodesCapacity;

  private final String viewId;

  private final String rcId;

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
    vmsConsumption = new IntMap(defConsumption);
    nodesCapacity = new IntMap(defCapacity);
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
    return vmsConsumption.get(vm.id());
  }

  /**
   * Get the node capacity.
   *
   * @param n the node
   * @return its capacity if it was defined otherwise the default value.
   */
  public int getCapacity(Node n) {
    return nodesCapacity.get(n.id());
  }

  /**
   * Get the VMs with defined consumptions.
   * @deprecated this operation is costly in terms of performance as it allocate VM objects.
   * Switch to {@link #forEachVmId(IntMap.Entry)} if needed.
   * @return a set that may be empty
   */
  @Deprecated
  public Set<VM> getDefinedVMs() {
    final Set<VM> vs = new HashSet<>();
    this.vmsConsumption.forEach((id, v) -> {
      vs.add(new VM(id));
      return true;
    });
    return vs;
  }

  /**
   * Iterate over the registered VMs.
   * The iterator just returns the element identifier, not a full VM object.
   *
   * @param e the entry.
   */
  public void forEachVMId(final IntMap.Entry e) {
    vmsConsumption.forEach(e);
  }

  /**
   * Iterate over the registered Nodes.
   * The iterator just returns the element identifier, not a full Node object.
   *
   * @param e the entry.
   */
  public void forEachNodeId(final IntMap.Entry e) {
    nodesCapacity.forEach(e);
  }

  /**
   * Get the nodes with defined capacities
   *
   * @return a set that may be empty
   * @deprecated this operation is costly in terms of performance as it allocate Node objects.
   * Switch to {@link #forEachNodeId(IntMap.Entry)} if needed.
   */
  @Deprecated
  public Set<Node> getDefinedNodes() {
    final Set<Node> ns = new HashSet<>();
    this.nodesCapacity.forEach((id, v) -> {
      ns.add(new Node(id));
      return true;
    });
    return ns;
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
    vmsConsumption.put(vm.id(), val);
    return this;
  }

  /**
   * Set the resource consumption of VMs.
   *
   * @param val the value to set
   * @param vms the VMs
   * @return the current resource
   */
  public ShareableResource setConsumption(int val, VM... vms) {
    Stream.of(vms).forEach(v -> setConsumption(v, val));
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
    nodesCapacity.put(n.id(), val);
    return this;
  }

  /**
   * Set the resource consumption of nodes.
   *
   * @param val   the value to set
   * @param nodes the nodes
   * @return the current resource
   */
  public ShareableResource setCapacity(int val, Node... nodes) {
    Stream.of(nodes).forEach(n -> this.setCapacity(n, val));
    return this;
  }

  /**
   * Unset a VM consumption.
   *
   * @param vm the VM
   */
  public void unset(VM vm) {
    vmsConsumption.clear(vm.id());
  }

  /**
   * Unset a node capacity.
   *
   * @param n the node
   */
  public void unset(Node n) {
    nodesCapacity.clear(n.id());
  }

  /**
   * Unset the consumption of VMs.
   *
   * @param vms the VMs
   */
  public void unset(VM... vms) {
    Stream.of(vms).forEach(this::unset);
  }

  /**
   * Unset the capacity of several nodes
   *
   * @param nodes the nodes
   */
  public void unset(Node... nodes) {
    Stream.of(nodes).forEach(this::unset);
  }

  /**
   * Check if the resource consumption is defined for a VM.
   *
   * @param vm the VM
   * @return {@code true} iff the consumption is defined.
   */
  public boolean consumptionDefined(VM vm) {
    return vmsConsumption.has(vm.id());
  }

  /**
   * Check if the resource capacity is defined for a node.
   *
   * @param n the node identifier
   * @return {@code true} iff the capacity is defined}.
   */
  public boolean capacityDefined(Node n) {
    return nodesCapacity.has(n.id());
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
    return vmsConsumption.noEntryValue();
  }

  /**
   * Get the default node capacity.
   *
   * @return the value.
   */
  public int getDefaultCapacity() {
    return nodesCapacity.noEntryValue();
  }

  /**
   * Prepare the backend used to store VM stats.
   * The backend will be expanded if needed. This operation is purely performance oriented as the backend grows
   * automatically whenever needed. Setting this value may just bypass the incremental memory allocation.
   *
   * @param nbVMs the estimated number of VMs to consider in the view.
   */
  public void minVMBackendCapacity(final int nbVMs) {
    this.vmsConsumption.expand(nbVMs);
  }

  /**
   * Prepare the backend used to store node stats.
   * The backend will be expanded if needed. This operation is purely performance oriented as the backend grows
   * automatically whenever needed. Setting this value may just bypass the incremental memory allocation.
   *
   * @param nbNodes the estimated number of nodes to consider in the view.
   */
  public void minNodeBackendCapacity(final int nbNodes) {
    this.nodesCapacity.expand(nbNodes);
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
    ShareableResource rc = new ShareableResource(rcId, nodesCapacity.noEntryValue(), vmsConsumption.noEntryValue());
    rc.nodesCapacity = nodesCapacity.copy();
    rc.vmsConsumption = vmsConsumption.copy();
    return rc;
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(",", String.format("rc:%s:", rcId), "");
    nodesCapacity.forEach((int k, int v) -> {
      joiner.add(String.format("<node %s,%d>", Node.toString(k), v));
      return true;
    });
    StringJoiner vmJoiner = new StringJoiner(",");
    vmsConsumption.forEach((int k, int v) -> {
      vmJoiner.add(String.format("<VM %s,%d>", VM.toString(k), v));
      return true;
    });
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
    for (VM u: ids) {
      if (consumptionDefined(u) || undef) {
        s += vmsConsumption.get(u.id());
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
    for (Node u: ids) {
      if (capacityDefined(u) || undef) {
        s += nodesCapacity.get(u.id());
      }
    }
    return s;
  }

  /**
   * Get the view associated to a model if exists.
   *
   * @param mo the model to look at
   * @param id the resource identifier
   * @return the view if attached. {@code null} otherwise
   */
  public static ShareableResource get(Model mo, String id) {
    return (ShareableResource) mo.getView(VIEW_ID_BASE + id);
  }
}
