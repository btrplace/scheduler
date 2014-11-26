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

package org.btrplace.btrpsl.template;

import org.btrplace.btrpsl.Script;
import org.btrplace.btrpsl.element.BtrpElement;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.model.Element;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.NamingService;

import java.util.Map;

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
     * @param e       the name of the element
     * @param options the options
     * @return a new element
     */
    BtrpElement check(Script scr, Element e, Map<String, String> options) throws ElementBuilderException;

    /**
     * Get the identifier associated to the template.
     *
     * @return a non-empty String
     */
    String getIdentifier();

    /**
     * Get the type of built elements.
     *
     * @return {@link org.btrplace.btrpsl.element.BtrpOperand.Type#VM} or {@link org.btrplace.btrpsl.element.BtrpOperand.Type#node}
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
