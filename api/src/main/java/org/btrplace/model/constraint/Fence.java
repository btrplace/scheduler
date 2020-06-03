/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A constraint to force the given VM, when running,
 * to be hosted on a given group of nodes.
 *
 * @author Fabien Hermenier
 */
@SideConstraint(args = {"v : vms", "ns <: nodes"}, inv = "vmState(v) = running --> host(v) : ns")
public class Fence extends SimpleConstraint {

  private final VM vm;

  private final Collection<Node> nodes;

    /**
     * Make a new discrete constraint.
     *
     * @param vm    the involved VM
     * @param nodes the involved nodes
     */
    public Fence(VM vm, Collection<Node> nodes) {
        this(vm, nodes, false);
    }

    /**
     * Make a new discrete constraint.
     *
     * @param vm the involved VM
     * @param n  the involved nodes
     */
    public Fence(VM vm, Node... n) {
        this(vm, Arrays.asList(n), false);
    }

    /**
     * Make a new constraint.
     *
     * @param vm         the VM identifiers
     * @param nodes      the nodes identifiers
     * @param continuous {@code true} for a continuous constraint.
     */
    public Fence(VM vm, Collection<Node> nodes, boolean continuous) {
        super(continuous);
        this.vm = vm;
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return "fence(vm=" + vm + ", nodes=" + nodes + ", " + (isContinuous() ? "continuous" : "discrete") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Fence fence = (Fence) o;
        return isContinuous() == fence.isContinuous() &&
                Objects.equals(vm, fence.vm) &&
                nodes.size() == fence.nodes.size() &&
                nodes.containsAll(fence.nodes) &&
                fence.nodes.containsAll(nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vm, nodes, isContinuous());
    }

    @Override
    public FenceChecker getChecker() {
        return new FenceChecker(this);
    }

    @Override
    public Collection<Node> getInvolvedNodes() {
        return nodes;
    }

    @Override
    public Collection<VM> getInvolvedVMs() {
        return Collections.singleton(vm);
    }

    /**
     * Instantiate discrete constraints for a collection of VMs.
     *
     * @param vms   the VMs to integrate
     * @param nodes the hosts to disallow
     * @return the associated list of constraints
     */
    public static List<Fence> newFence(Collection<VM> vms, Collection<Node> nodes) {
        return vms.stream().map(v -> new Fence(v, nodes)).collect(Collectors.toList());
    }
}
