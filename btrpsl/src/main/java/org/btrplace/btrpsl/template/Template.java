/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.template;

import org.btrplace.btrpsl.element.BtrpElement;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.NamingService;

/**
 * A Template is a skeleton for a node or a virtual machine.
 * Each call to check() generate a new unique element that can takes
 * some properties from its template.
 *
 * @author Fabien Hermenier
 */
public interface Template {

    /**
     * Build a new element that inherit from a template.
     *
     * @return a new element
     * @throws ElementBuilderException if an error occurred while building the element
     */
    BtrpElement check() throws ElementBuilderException;

    /**
     * Get the identifier associated to the template.
     *
     * @return a non-empty String
     */
    String getIdentifier();

    /**
     * Get the type of built elements.
     *
     * @return {@link org.btrplace.btrpsl.element.BtrpOperand.Type#VM} or {@link org.btrplace.btrpsl.element.BtrpOperand.Type#NODE}
     */
    BtrpOperand.Type getElementType();

    /**
     * Set the node naming service to use for that template.
     *
     * @param srvNodes the service to use
     */
    void setNamingServiceNodes(NamingService<Node> srvNodes);

    /**
     * Set the vm naming service to use for that template.
     *
     * @param srvVMs the service to use
     */
    void setNamingServiceVMs(NamingService<VM> srvVMs);

}
