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

package btrplace.btrpsl;

import btrplace.btrpsl.element.BtrpElement;
import btrplace.model.Element;
import btrplace.model.view.ModelView;

import java.util.Set;

/**
 * A service to declare VMs and track their fully-qualified name
 *
 * @author Fabien Hermenier
 */
public interface NamingService extends ModelView {

    /**
     * The view identifier.
     */
    final String ID = "btrpsl.ns";

    /**
     * Declare an element.
     *
     * @param id the element identifier. Starts with a {@code \@} to indicate
     *           a node. Otherwise, the element will be considered as a virtual machine
     * @param e  the element to register
     * @return the registered element if the operation succeed. {@code null} otherwise
     */
    BtrpElement register(String id, Element e) throws NamingServiceException;

    /**
     * Get the element associated to a given identifier.
     *
     * @param n the element identifier
     * @return the matching element if any, {@code null} otherwise
     */
    BtrpElement resolve(String n);

    /**
     * Get the fully qualified name of a given model element.
     *
     * @param el the element
     * @return a String if the name can be resolved
     */
    String resolve(Element el);

    /**
     * Get all the registered elements.
     *
     * @return a set of elements. May be empty
     */
    Set<Element> getRegisteredElements();
}
