/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

import org.btrplace.Copyable;

/**
 * Interface to specify a builder to create node or VMs.
 * Each created element is guarantee for being unique wrt. all the
 * created element with the same type.
 *
 * @author Fabien Hermenier
 */
public interface ElementBuilder extends Copyable<ElementBuilder> {

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
}
