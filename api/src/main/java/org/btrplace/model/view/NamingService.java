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

package org.btrplace.model.view;

import org.btrplace.model.Element;
import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A simple service to name VMs or nodes.
 * For a given type of element, the name must be unique
 *
 * @author Fabien Hermenier
 */
public final class NamingService<E extends Element> implements ModelView {

    private Map<String, E> resolve;
    private Map<E, String> rev;

    /**
     * The root view identifier.
     * Will be suffixed by either "vm" or "node
     */
    public static final String ID = "btrpsl.ns.";

    private String elemId;

    /**
     * Make a naming service dedicated to nodes.
     *
     * @return a new naming service
     */
    public static NamingService<Node> newNodeNS() {
        return new NamingService<>("node");
    }

    /**
     * Make a naming service dedicated to VMs.
     *
     * @return a new naming service
     */
    public static NamingService<VM> newVMNS() {
        return new NamingService<>("vm");
    }

    /**
     * Make a new service.
     *
     * @param eId "vm" or "node"
     */
    private NamingService(String eId) {
        resolve = new HashMap<>();
        rev = new HashMap<>();
        this.elemId = eId;
    }

    @Override
    public String getIdentifier() {
        return ID + elemId;
    }

    /**
     * Get the element identifier of this naming service
     *
     * @return "vm" or "node" for a naming service dedicated to the VMs or the nodes respectively
     */
    public String getElementIdentifier() {
        return elemId;
    }

    /**
     * Register the name of an element.
     *
     * @param e    the element to register
     * @param name the element name. Must be globally unique
     * @return {@code true} if the association has been established, {@code false} if the name is already used
     * for another element
     */
    public boolean register(E e, String name) {
        if (resolve.containsKey(name)) {
            return false;
        }
        resolve.put(name, e);
        rev.put(e, name);
        return true;
    }

    /**
     * Get the element associated to a given name.
     *
     * @param name the element name
     * @return the associated element if exists, {@code null} otherwise
     */
    public E resolve(String name) {
        return resolve.get(name);
    }

    /**
     * Get the name of a given element.
     *
     * @param e the element
     * @return the element unique name if exists. {@code null} otherwise
     */
    public String resolve(E e) {
        return rev.get(e);
    }

    /**
     * Get all the registered elements.
     *
     * @return a set of elements that may be empty.
     */
    public Set<E> getNamedElements() {
        return rev.keySet();
    }

    /**
     * Re-associate the name of a registered VM to a new VM.
     *
     * @param curId  the current VM identifier
     * @param nextId the new VM identifier
     * @return {@code true} if the re-association succeeded or if there is no VM {@code curId} registered.
     * {@code false} if {@code nextId} is already associated to a name
     */
    @Override
    public boolean substituteVM(VM curId, VM nextId) {
        if (elemId.equals("vm")) {
            if (rev.containsKey(nextId)) {
                //the new id already exists. It is a failure scenario.
                return false;
            }

            String fqn = rev.remove(curId);
            if (fqn != null) {
                //new resolution, with the substitution of the old one.
                rev.put((E) nextId, fqn);
                resolve.put(fqn, (E) nextId);
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NamingService that = (NamingService) o;

        return elemId.equals(that.elemId) && resolve.equals(that.resolve);
    }

    @Override
    public NamingService<E> clone() {
        NamingService<E> cpy = new NamingService<>(elemId);
        for (Map.Entry<String, E> e : resolve.entrySet()) {
            cpy.resolve.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<E, String> e : rev.entrySet()) {
            cpy.rev.put(e.getKey(), e.getValue());
        }
        return cpy;
    }


    @Override
    public int hashCode() {
        return resolve.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Iterator<Map.Entry<E, String>> ite = rev.entrySet().iterator(); ite.hasNext(); ) {
            Map.Entry<E, String> e = ite.next();
            b.append(e.getKey()).append("<->").append(e.getValue());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.toString();
    }

}
