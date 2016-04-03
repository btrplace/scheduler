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

package org.btrplace.model;

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
     * Check if a given VM has been defined for this model.
     *
     * @param v the VM to check
     * @return {@code true} iff the VM is already defined
     */
    boolean contains(VM v);

    /**
     * Check if a given node has been defined for this model.
     *
     * @param n the node to check
     * @return {@code true} iff the VM is booked
     */
    boolean contains(Node n);

    /**
     * Clone the builder.
     *
     * @return a new element builder
     */
    ElementBuilder clone();

}
