/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.view;

import org.btrplace.model.Element;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

/**
 * A simple service to name VMs or nodes.
 * For a given type of element, the name must be unique
 *
 * @author Fabien Hermenier
 */
public final class NamingService<E extends Element> implements ModelView {

    private final Map<String, E> resolve;
    private final Map<E, String> rev;

    /**
     * The root view identifier.
     * Will be suffixed by either {@link Node#TYPE} or {@link VM#TYPE}.
     */
    public static final String ID = "btrpsl.ns.";

    private final String elemId;

    /**
     * Make a new service.
     *
     * @param eId {@link VM#TYPE} or {@link Node#TYPE}
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
     * @return {@link VM#TYPE} or {@link Node#TYPE} for a naming service dedicated to the VMs or the nodes respectively
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
        if (VM.TYPE.equals(elemId)) {
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

        NamingService<?> that = (NamingService<?>) o;

        return elemId.equals(that.elemId) && resolve.equals(that.resolve);
    }

    @Override
    public NamingService<E> copy() {
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
        StringJoiner joiner = new StringJoiner(", ");
        for (Entry<E, String> entry : rev.entrySet()) {
            joiner.add(String.format("%s<->%s", entry.getKey(), entry.getValue()));
        }
        return joiner.toString();
    }

    /**
     * Make a naming service dedicated to nodes.
     *
     * @return a new naming service
     */
    public static NamingService<Node> newNodeNS() {
        return new NamingService<>(Node.TYPE);
    }

    /**
     * Make a naming service dedicated to VMs.
     *
     * @return a new naming service
     */
    public static NamingService<VM> newVMNS() {
        return new NamingService<>(VM.TYPE);
    }

    /**
     * Get the naming service for the VMs associated to a model.
     *
     * @param mo the model to look at
     * @return the view if attached. {@code null} otherwise
     */
    @SuppressWarnings("unchecked")
    public static NamingService<VM> getVMNames(Model mo) {
        return (NamingService<VM>) mo.getView(ID + VM.TYPE);
    }

    /**
     * Get the naming service for the nodes associated to a model.
     *
     * @param mo the model to look at
     * @return the view if attached. {@code null} otherwise
     */
    @SuppressWarnings("unchecked")
    public static NamingService<Node> getNodeNames(Model mo) {
        return (NamingService<Node>) mo.getView(ID + Node.TYPE);
    }
}
