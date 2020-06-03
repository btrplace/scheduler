/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A constraint to disallow the given VM, when running,
 * to be hosted on a given set of nodes.
 *
 * @author Fabien Hermenier
 */
@SideConstraint(args = {"v : vms", "ns <: nodes"}, inv = "vmState(v) = running --> host(v) /: ns")
public class Ban extends SimpleConstraint {

  private final VM vm;

  private final Collection<Node> nodes;

    /**
     * Make a new discrete constraint.
     *
     * @param vm    the VM identifiers
     * @param nodes the nodes identifiers
     */
    public Ban(VM vm, Collection<Node> nodes) {
        this(vm, nodes, false);
    }

    /**
     * Make a new constraint.
     *
     * @param vm         the VM identifiers
     * @param nodes      the nodes identifiers
     * @param continuous {@code true} for a continuous constraint.
     */
    public Ban(VM vm, Collection<Node> nodes, boolean continuous) {
        super(continuous);
        this.vm = vm;
        this.nodes = nodes;
    }

    /**
     * Make a new constraint discrete constraint.
     *
     * @param vm   the VM identifier
     * @param node the node identifier
     */
    public Ban(VM vm, final Node node) {
        this(vm, Collections.singleton(node));
    }

    @Override
    public Collection<Node> getInvolvedNodes() {
        return nodes;
    }

    @Override
    public Collection<VM> getInvolvedVMs() {
        return Collections.singleton(vm);
    }

    @Override
    public String toString() {
        return "ban(" + "vm=" + vm + ", nodes=" + nodes + ", " + (isContinuous() ? "continuous" : "discrete") + ")";
    }

    @Override
    public BanChecker getChecker() {
        return new BanChecker(this);
    }

    /**
     * Instantiate discrete constraints for a collection of VMs.
     *
     * @param vms   the VMs to integrate
     * @param nodes the hosts to disallow
     * @return the associated list of constraints
     */
    public static List<Ban> newBan(Collection<VM> vms, Collection<Node> nodes) {
        return vms.stream().map(v -> new Ban(v, nodes)).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Ban ban = (Ban) o;
        return isContinuous() == ban.isContinuous() &&
                Objects.equals(vm, ban.vm) &&
                nodes.size() == ban.nodes.size() &&
                nodes.containsAll(ban.nodes) &&
                ban.nodes.containsAll(nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vm, nodes, isContinuous());
    }
}
