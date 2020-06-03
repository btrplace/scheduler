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
 * A constraint to force sets of running VMs to be hosted on distinct set of nodes.
 * VMs inside a same set may still be collocated.
 * <p>
 * The set of VMs must be disjoint so must be the set of servers.
 * <p>
 * If the constraint is set to provide a discrete restriction, it only ensures no group of VMs share a group of nodes
 * while each group of VMs does not spread over several group of nodes. This allows to change the group of nodes
 * hosting the group of VMs during the reconfiguration process.
 * <p>
 * If the constraint is set to provide a continuous restriction, the constraint must be satisfied initially, then the VMs
 * of a single group can never spread on multiple groups of nodes nor change of group.
 *
 * @author Fabien Hermenier
 */
@SideConstraint(args = {"vs <<: vms", "part <<: nodes"}, inv = "(!(v : vs) Among(v, part)) & Split(vs)")
public class SplitAmong extends SimpleConstraint {

  /**
   * Set of set of vms.
   */
  private final Collection<Collection<VM>> vGroups;

    /**
     * Set of set of nodes.
     */
    private final Collection<Collection<Node>> pGroups;

  /**
     * Make a new constraint having a discrete restriction.
     *
     * @param vParts the disjoint sets of VMs
     * @param pParts the disjoint sets of nodes.
     */
    public SplitAmong(Collection<Collection<VM>> vParts, Collection<Collection<Node>> pParts) {
        this(vParts, pParts, false);
    }


    /**
     * Make a new constraint.
     *
     * @param vParts     the disjoint sets of VMs
     * @param pParts     the disjoint sets of nodes.
     * @param continuous {@code true} for a continuous restriction
     */
    public SplitAmong(Collection<Collection<VM>> vParts, Collection<Collection<Node>> pParts, boolean continuous) {
        super(continuous);
        int cnt = 0;
        Set<Node> all = new HashSet<>();
        for (Collection<Node> s : pParts) {
            cnt += s.size();
            all.addAll(s);
            if (cnt != all.size()) {
                throw new IllegalArgumentException("The constraint expects disjoint sets of nodes");
            }
        }

        this.vGroups = vParts;
        this.pGroups = pParts;
    }

    @Override
    public Set<VM> getInvolvedVMs() {
        Set<VM> s = new HashSet<>();
        vGroups.forEach(s::addAll);
        return s;
    }

    @Override
    public Set<Node> getInvolvedNodes() {
        Set<Node> s = new HashSet<>();
        pGroups.forEach(s::addAll);
        return s;
    }

    /**
     * Get the groups of VMs identifiers
     *
     * @return the groups
     */
    public Collection<Collection<VM>> getGroupsOfVMs() {
        return vGroups;
    }

    /**
     * Get the groups of nodes identifiers
     *
     * @return the groups
     */
    public Collection<Collection<Node>> getGroupsOfNodes() {
        return pGroups;
    }

    /**
     * Get the group of nodes associated to a given node.
     *
     * @param u the node
     * @return the associated group of nodes if exists. An empty set otherwise
     */
    public Collection<Node> getAssociatedPGroup(Node u) {
        for (Collection<Node> pGrp : pGroups) {
            if (pGrp.contains(u)) {
                return pGrp;
            }
        }
        return Collections.emptySet();
    }

    /**
     * Get the group of VMs associated to a given VM.
     *
     * @param u the VM
     * @return the associated group of VMs if exists.  An empty set otherwise
     */
    public Collection<VM> getAssociatedVGroup(VM u) {
        for (Collection<VM> vGrp : vGroups) {
            if (vGrp.contains(u)) {
                return vGrp;
            }
        }
        return Collections.emptySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SplitAmong that = (SplitAmong) o;
        return isContinuous() == that.isContinuous() &&
                Objects.equals(vGroups, that.vGroups) &&
                Objects.equals(pGroups, that.pGroups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vGroups, pGroups, isContinuous());
    }

    @Override
    public String toString() {
        return "splitAmong(" + "vms=[" + vGroups + ", nodes=" + pGroups + ", " + (isContinuous() ? "continuous" : "discrete") + ')';
    }


    @Override
    public SplitAmongChecker getChecker() {
        return new SplitAmongChecker(this);
    }

}
