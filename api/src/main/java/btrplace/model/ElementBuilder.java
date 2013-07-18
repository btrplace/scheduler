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

package btrplace.model;

import java.util.Set;

/**
 * Interface to specify a builder to create node or VMs.
 * Each created element is guarantee for being unique wrt. all the
 * created element with the same type.
 *
 * @author Fabien Hermenier
 */
public interface ElementBuilder {

    /**
     * Generate a new VM.
     *
     * @return {@code null} if no identifiers are available for the VM.
     */
    VM newVM();

    /**
     * Generate a new VM.
     *
     * @param id the identifier to use for that VM
     * @return a VM or {@code null} if the identifier is already used
     */
    VM newVM(int id);

    /**
     * Generate a new Node for this model.
     * The node will not be included in the mapping associated to the model.
     *
     * @return {@code null} if no identifiers are available for the Node.
     */
    Node newNode();

    /**
     * Generate a new node for this model.
     * The node will not be included in the mapping associated to the model.
     *
     * @param id the identifier to use for that node
     * @return a Node or {@code null} if the identifier is already used
     */
    Node newNode(int id);

    /**
     * Get all the registered nodes.
     *
     * @return a set of nodes, may be empty
     */
    Set<Node> getNodes();

    /**
     * Get all the registered VMs.
     *
     * @return a set of VMs, may be empty
     */
    Set<VM> getVMs();

    /**
     * Clone the builder.
     *
     * @return a new element builder
     */
    ElementBuilder clone();

}
