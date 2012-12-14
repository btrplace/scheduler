/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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
 * Common tools to manipulate a mapping.
 *
 * @author Fabien Hermenier
 */
public final class MappingUtils {

    /**
     * Enumeration to specify the state of the nodes to focus on.
     */
    public static enum State {
        /**
         * Specify running virtual machines.
         */
        Runnings,

        /**
         * Specify sleeping virtual machines.
         */
        Sleepings
    }

    /**
     * Utility class. No instantiation.
     */
    private MappingUtils() {
    }

    /**
     * Return the subset of online nodes that host at least one virtual machines
     *
     * @param cfg the mapping to browse
     * @param wrt the hosting type to consider
     * @return subset of node that may be empty
     */
    public static Set<UUID> usedNodes(Mapping cfg, EnumSet<State> wrt) {
        Set<UUID> ns = new HashSet<UUID>();

        if (wrt.contains(State.Runnings)) {
            for (UUID vm : cfg.getRunningVMs()) {
                ns.add(cfg.getVMLocation(vm));
            }
        }
        if (wrt.contains(State.Sleepings)) {
            for (UUID vm : cfg.getSleepingVMs()) {
                ns.add(cfg.getVMLocation(vm));
            }
        }

        return ns;
    }

    /**
     * Return the subset of online nodes that does not host virtual machines.
     *
     * @param cfg the mapping to browse
     * @param wrt the hosting type to consider
     * @return a subset of node that may be empty
     */
    public static Set<UUID> unusedNodes(Mapping cfg, State wrt) {
        Set<UUID> ns = new HashSet<UUID>();
        for (UUID n : cfg.getOnlineNodes()) {
            if (wrt == State.Runnings && cfg.getRunningVMs(n).isEmpty()) {
                ns.add(n);
            }
            if (wrt == State.Sleepings && cfg.getSleepingVMs(n).isEmpty()) {
                ns.add(n);
            }

        }
        return ns;
    }

    /**
     * Compute a sub mapping that only consider a subset of nodes.
     * ready VMs are ignored.
     *
     * @param nodes the subset of nodes
     * @return the sub mapping is the operation succeed, {@code null} otherwise
     */
    public static Mapping subMapping(Mapping cfg, Set<UUID> nodes) {
        Mapping d = new DefaultMapping();
        for (UUID n : nodes) {
            if (cfg.getOnlineNodes().contains(n)) {
                d.addOnlineNode(n);
                for (UUID vm : cfg.getRunningVMs(n)) {
                    d.addRunningVM(vm, n);
                }
                for (UUID vm : cfg.getSleepingVMs(n)) {
                    d.addSleepingVM(vm, n);
                }
            } else if (cfg.getOfflineNodes().contains(n)) {
                d.addOfflineNode(n);
            } else {
                return null;
            }
        }
        return d;
    }

    /**
     * Compute a sub mapping that only consider a subset of nodes and a subset of VMs.
     * To be valid, the subset of VMs must be hosted on the subset of nodes or be the part of the ready VMs.
     *
     * @param nodes the subset of nodes
     * @param vms   the subset of VMs
     * @return the sub mapping is the operation succeed, {@code null} otherwise
     */
    public static Mapping subMapping(Mapping cfg, Set<UUID> nodes, Set<UUID> vms) {
        Mapping d = new DefaultMapping();

        //Copy the nodes, unknown nodes lead to an error
        for (UUID n : nodes) {
            if (cfg.getOnlineNodes().contains(n)) {
                d.addOnlineNode(n);
            } else if (cfg.getOfflineNodes().contains(n)) {
                d.addOfflineNode(n);
            } else {
                return null;
            }
        }
        //Copy the VMs, unknown VMs lead to an error
        //If the VMs in on a node in cfg, this node must already be online in d
        for (UUID vm : vms) {
            if (cfg.getReadyVMs().contains(vm)) {
                d.addReadyVM(vm);
            } else if (cfg.getRunningVMs().contains(vm) && d.getOnlineNodes().contains(cfg.getVMLocation(vm))) {
                d.addRunningVM(vm, cfg.getVMLocation(vm));
            } else if (cfg.getSleepingVMs().contains(vm) && d.getOnlineNodes().contains(cfg.getVMLocation(vm))) {
                d.addSleepingVM(vm, cfg.getVMLocation(vm));
            }
        }
        return d;
    }

    /**
     * Check a suite of mapping are disjoint.
     * Two mappings are considered as disjoint when they do not have any nodes and VMs in common.
     *
     * @param cfgs the mapping to check
     * @return {@code true} iff all the mapping are disjoint
     */
    public static boolean areDisjoint(Collection<Mapping> cfgs) {
        return areDisjoint(cfgs.toArray(new Mapping[cfgs.size()]));
    }

    /**
     * Check a suite of mapping are disjoint.
     * Two mappings are considered as disjoint when they do not have any nodes and VMs in common.
     *
     * @param cfgs the mapping to check
     * @return {@code true} iff all the mapping are disjoint
     */
    public static boolean areDisjoint(Mapping... cfgs) {
        Set<UUID> nodes = new HashSet<UUID>();
        Set<UUID> vms = new HashSet<UUID>();
        for (Mapping cfg : cfgs) {
            Set<UUID> curNodes = cfg.getAllNodes();
            Set<UUID> curVMs = cfg.getAllVMs();
            if (!Collections.disjoint(nodes, curNodes)
                    || !Collections.disjoint(vms, curVMs)) {
                return false;
            }
            nodes.addAll(curNodes);
            vms.addAll(curVMs);
        }
        return true;
    }

    /**
     * Merge a collection of mappings.
     * The mappings are expected to be disjoint
     *
     * @param cfgs the list of mappings to merge
     * @return the resulting mapping
     */
    public static Mapping merge(Collection<Mapping> cfgs) {
        return merge(cfgs.toArray(new Mapping[cfgs.size()]));
    }

    /**
     * Merge a collection of mappings.
     * The mappings are expected to be disjoint
     *
     * @param cfgs the list of mappings to merge
     * @return the resulting mapping or {@code null} if the merge was not possible
     */
    public static Mapping merge(Mapping... cfgs) {
        Mapping res = new DefaultMapping();
        for (Mapping c : cfgs) {
            for (UUID n : c.getOfflineNodes()) {
                if (res.getAllNodes().contains(n)) {
                    return null;
                }
                if (!res.addOfflineNode(n)) {
                    return null;
                }
            }
            for (UUID n : c.getOnlineNodes()) {
                if (res.containsNode(n)) {
                    return null;
                }
                res.addOnlineNode(n);
                for (UUID vm : c.getRunningVMs(n)) {
                    if (res.containsVM(vm)) {
                        return null;
                    }
                    if (!res.addRunningVM(vm, n)) {
                        return null;
                    }
                }
                for (UUID vm : c.getSleepingVMs(n)) {
                    if (res.containsVM(vm)) {
                        return null;
                    }
                    if (!res.addSleepingVM(vm, n)) {
                        return null;
                    }
                }
            }
            for (UUID vm : c.getReadyVMs()) {
                if (res.containsVM(vm)) {
                    return null;
                }
                res.addReadyVM(vm);
            }
        }
        return res;
    }
}
