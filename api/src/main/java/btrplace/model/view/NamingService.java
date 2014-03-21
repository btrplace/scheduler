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

package btrplace.model.view;

import btrplace.model.Element;
import btrplace.model.VM;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A simple service to name elements.
 * This service allows to associate a name to any element.
 * The namespace is the same for all the kind of elements so it is not possible to
 * have a node and a VM with a same name.
 *
 * @author Fabien Hermenier
 */
public class NamingService implements ModelView {

    private Map<String, Element> resolve;

    private Map<Element, String> rev;

    /**
     * The view identifier.
     */
    public static final String ID = "btrpsl.ns";

    /**
     * Make a new service.
     */
    public NamingService() {
        resolve = new HashMap<>();
        rev = new HashMap<>();
    }

    @Override
    public String getIdentifier() {
        return ID;
    }

    /**
     * Register the name of an element.
     *
     * @param e    the element to register
     * @param name the element name. Must be globally unique
     * @return {@code true} if the association has been established, {@code false} if the name is already used
     * for another element
     */
    public boolean register(Element e, String name) {
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
    public Element resolve(String name) {
        return resolve.get(name);
    }

    /**
     * Get the name of a given element.
     *
     * @param e the element
     * @return the element unique name if exists. {@code null} otherwise
     */
    public String resolve(Element e) {
        return rev.get(e);
    }

    /**
     * Get all the registered names.
     *
     * @return a set of names that may be empty.
     */
    public Set<String> getRegisteredNames() {
        return resolve.keySet();
    }

    /**
     * Get all the registered elements.
     *
     * @return a set of elements that may be empty.
     */
    public Set<Element> getRegisteredElements() {
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
        if (rev.containsKey(nextId)) {
            //the new id already exists. It is a failure scenario.
            return false;
        }

        String fqn = rev.remove(curId);
        if (fqn != null) {
            //new resolution, with the substitution of the old one.
            rev.put(nextId, fqn);
            resolve.put(fqn, nextId);

        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NamingService)) {
            return false;
        }

        NamingService that = (NamingService) o;
        if (!getRegisteredNames().equals(that.getRegisteredNames())) {
            return false;
        }
        for (String s : getRegisteredNames()) {
            Element e = resolve(s);
            if (!s.equals(that.resolve(e))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NamingService clone() {
        NamingService cpy = new NamingService();
        for (Map.Entry<String, Element> e : resolve.entrySet()) {
            cpy.resolve.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<Element, String> e : rev.entrySet()) {
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
        for (Iterator<Map.Entry<Element, String>> ite = rev.entrySet().iterator(); ite.hasNext(); ) {
            Map.Entry<Element, String> e = ite.next();
            b.append('<').append(e.getKey()).append(" : ").append(e.getValue()).append('>');
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.toString();
    }
}
