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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A constraint to force a set of VMs to be hosted on a single group of nodes
 * among those available.
 * <p>
 * When the restriction is discrete, the constraint only ensure that the VMs are not spread over several
 * group of nodes at the end of the reconfiguration process. However, this situation may occur temporary during
 * the reconfiguration. Basically, this allows to select a new group of nodes for the VMs.
 * <p>
 * When the restriction is continuous, if some VMs are already running, on a group of nodes,
 * it will not be possible to relocated the VMs to a new group of nodes.
 *
 * @author Fabien Hermenier
 */
@SideConstraint(args = {"vs <: vms", "parts <<: nodes"}, inv = "?(g : parts) {host(i). i : vs, vmState(i) = running} <: g")
public class Among extends SimpleConstraint {

  /**
   * Set of set of nodes.
   */
  private final Collection<Collection<Node>> pGroups;


    private final Collection<VM> vms;

  /**
     * Make a new constraint with a discrete restriction.
     *
     * @param vms   the group of VMs
     * @param parts disjoint set of nodes
     */
    public Among(Collection<VM> vms, Collection<Collection<Node>> parts) {
        this(vms, parts, false);

    }

    /**
     * Make a new constraint.
     *
     * @param vms        the group of VMs
     * @param parts      disjoint set of nodes
     * @param continuous {@code true} for a continuous restriction
     */
    @SuppressWarnings("squid:S3346")
    public Among(Collection<VM> vms, Collection<Collection<Node>> parts, boolean continuous) {
        super(continuous);
        assert checkDisjoint(parts) : "The constraint expects disjoint sets of nodes";
        this.vms = vms;
        this.pGroups = parts;
    }

    private static boolean checkDisjoint(Collection<Collection<Node>> parts) {
        Set<Node> all = new HashSet<>();
        int cnt = 0;
        for (Collection<Node> s : parts) {
            cnt += s.size();
            all.addAll(s);
            if (cnt != all.size()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the group of nodes that contains the given node.
     *
     * @param u the node identifier
     * @return the group of nodes if exists. An empty collection otherwise
     */
    public Collection<Node> getAssociatedPGroup(Node u) {
        for (Collection<Node> pGrp : pGroups) {
            if (pGrp.contains(u)) {
                return pGrp;
            }
        }
        return Collections.emptySet();
    }

    @Override
    public Collection<Node> getInvolvedNodes() {
        Set<Node> s = new HashSet<>();
        pGroups.forEach(s::addAll);
        return s;
    }

    @Override
    public Collection<VM> getInvolvedVMs() {
        return vms;
    }

    /**
     * Get the groups of nodes identifiers
     *
     * @return the groups
     */
    public Collection<Collection<Node>> getGroupsOfNodes() {
        return pGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Among among = (Among) o;
        return isContinuous() == among.isContinuous() &&
                Objects.equals(pGroups, among.pGroups) &&
                Objects.equals(vms, among.vms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pGroups, vms, isContinuous());
    }

    @Override
    public String toString() {
        return "among(vms=" + vms + ", nodes=" + pGroups + ", " + (isContinuous() ? "continuous" : "discrete") + ")";
    }

    @Override
    public AmongChecker getChecker() {
        return new AmongChecker(this);
    }
}
