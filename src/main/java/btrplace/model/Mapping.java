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

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * A mapping denotes the current state and placement of virtual machines and nodes.
 * These elements are identified through their UUID
 *
 * @author Fabien Hermenier
 */
public interface Mapping extends Cloneable {

    /**
     * Set a virtual machine running on a node. The node must already be online.
     * If the virtual machine is already in a other location or state in the mapping, it is updated
     *
     * @param vm   the VM identifier
     * @param node the node that will host the virtual machine. Must be considered as online.
     * @return {@code true} if the vm is assigned on the node.
     */
    boolean addRunningVM(UUID vm, UUID node);

    /**
     * Set a virtual machine sleeping on a node.
     * If the virtual machine is already in a other location or state in the mapping, it is updated
     *
     * @param vm   the virtual machine
     * @param node the node that will host the virtual machine. Must be considered as online.
     * @return {@code false} if the hosting node is offline or unknown
     */
    boolean addSleepingVM(UUID vm, UUID node);

    /**
     * Set a virtual machine ready for being running.
     * If the virtual machine is already in a other location or state in the mapping, it is updated
     *
     * @param vm the virtual machine
     */
    void addReadyVM(UUID vm);

    /**
     * Remove a virtual machine.
     *
     * @param vm the virtual machine to remove
     * @return {@code true} if the VM was in the mapping and has been removed
     */
    boolean removeVM(UUID vm);

    /**
     * Remove a node. The node must not host any virtual machines
     *
     * @param n the node to remove
     * @return {@code true} if the node was in the mapping and is removed. {@code false} otherwise
     */
    boolean removeNode(UUID n);

    /**
     * Get the list of nodes that are online.
     *
     * @return a list, may be empty
     */
    Set<UUID> getOnlineNodes();

    /**
     * Set a node online. If the node is already in the mapping but in an another state, it is updated.
     *
     * @param node the node to add
     */
    void addOnlineNode(UUID node);

    /**
     * Set a node offline. If the node is already in the mapping but in an another state, it is updated.
     * The node must not host any virtual machines
     *
     * @param node the node
     * @return true if the node is offline. False otherwise
     */
    boolean addOfflineNode(UUID node);

    /**
     * Get the nodes that are offline.
     *
     * @return a list of nodes, may be empty
     */
    Set<UUID> getOfflineNodes();


    /**
     * Get the virtual machines that are running.
     *
     * @return a set of UUIDs, may be empty
     */
    Set<UUID> getRunningVMs();

    /**
     * Get the virtual machines that are sleeping.
     *
     * @return a set of virtual machines, may be empty
     */
    Set<UUID> getSleepingVMs();

    /**
     * Get the virtual machines that are sleeping on a node.
     *
     * @param n the node
     * @return a set of virtual machines, may be empty
     */
    Set<UUID> getSleepingVMs(UUID n);

    /**
     * Get the virtual machines that are running on a node.
     *
     * @param n the node
     * @return a set of virtual machines, may be empty
     */
    Set<UUID> getRunningVMs(UUID n);

    /**
     * Get the virtual machines that are ready.
     *
     * @return a list, may be empty
     */
    Set<UUID> getReadyVMs();

    /**
     * Get all the virtual machines involved in the mapping.
     *
     * @return a set, may be empty
     */
    Set<UUID> getAllVMs();

    /**
     * Get all the nodes involved in the mapping.
     *
     * @return a set, may be empty
     */
    Set<UUID> getAllNodes();

    /**
     * Get the location of a  running or a sleeping virtual machine.
     *
     * @param vm the virtual machine
     * @return the node hosting the virtual machine or {@code null} is the virtual machine
     *         is not in the sleeping state nor the running state
     */
    UUID getVMLocation(UUID vm);

    /**
     * Get all the virtual machines running on a collection of nodes.
     *
     * @param ns the set of nodes
     * @return a set of virtual machines, may be empty
     */
    Set<UUID> getRunningVMs(Collection<UUID> ns);

    /**
     * Copy a mapping.
     *
     * @return the created copy
     */
    Mapping clone();

    /**
     * Check if a virtual machine is in the mapping.
     *
     * @param vm the virtual machine identifier
     * @return {@code true} if the virtual machine is in.
     */
    boolean containsVM(UUID vm);

    /**
     * Check if a node is in the mapping.
     *
     * @param node the node identifier
     * @return {@code true} if the node is in.
     */
    boolean containsNode(UUID node);

    /**
     * Remove all the nodes and the VMs in the mapping.
     */
    void clear();

    /**
     * Remove all the VMs remove on a given node
     *
     * @param u the node identifier
     */
    void clearNode(UUID u);

    /**
     * Remove all the VMs in the mapping
     */
    void clearAllVMs();
}
