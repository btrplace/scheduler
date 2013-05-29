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
 * Allow to specify attributes related to managed elements.
 * Attributes are key/value pair, where values are Java primitives (integer, double, String, boolean)
 *
 * @author Fabien Hermenier
 */
public interface Attributes extends Cloneable {

    /**
     * Put a boolean value for a VM.
     *
     * @param vm the VM
     * @param k  the attribute identifier
     * @param b  the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(VM vm, String k, boolean b);

    /**
     * Put a String value for a VM.
     *
     * @param vm the VM
     * @param k  the attribute identifier
     * @param s  the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(VM vm, String k, String s);

    /**
     * Put a double value.
     *
     * @param vm the VM
     * @param k  the attribute identifier
     * @param d  the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(VM vm, String k, double d);

    /**
     * Put an integer value.
     *
     * @param vm the VM
     * @param k  the attribute identifier
     * @param d  the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(VM vm, String k, int d);

    /**
     * Get an attribute value as a simple Object.
     *
     * @param vm the VM
     * @param k  the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Object get(VM vm, String k);

    /**
     * Get an attribute value as a boolean.
     *
     * @param vm the VM
     * @param k  the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Boolean getBoolean(VM vm, String k);

    /**
     * Get an attribute value as a string.
     *
     * @param vm the VM
     * @param k  the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    String getString(VM vm, String k);

    /**
     * Get an attribute value as a double.
     *
     * @param vm the VM
     * @param k  the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Double getDouble(VM vm, String k);

    /**
     * Get an attribute value as an integer.
     *
     * @param vm the VM
     * @param k  the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Integer getInteger(VM vm, String k);

    /**
     * Check if an attribute is set for a given element.
     *
     * @param vm the VM
     * @param k  the attribute identifier
     * @return {@code true} iff the attribute is set
     */
    boolean isSet(VM vm, String k);

    /**
     * Unset an attribute for a given element.
     *
     * @param vm the VM
     * @param k  the attribute identifier
     * @return {@code true} iff a value was removed
     */
    boolean unset(VM vm, String k);

    /**
     * Clone the attributes.
     *
     * @return a new set of attributes
     */
    Attributes clone();

    /**
     * Get the VMs having attributes defined.
     *
     * @return a set that may be empty
     */
    Set<VM> getSpecifiedVMs();

    /**
     * Get all the attributes keys that are registered for a VM.
     *
     * @param vm the VM
     * @return a set that may be empty
     */
    Set<String> getKeys(VM vm);

    /**
     * Put a value but try to cast into to a supported primitive if possible.
     * First, it tries to cast {@code v} first to a boolean, then to a integer value,
     * finally to a double value. If none of the cast succeeded, the value is let
     * as a string.
     *
     * @param vm the VM
     * @param k  the attribute identifier
     * @param v  the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean castAndPut(VM vm, String k, String v);

    /**
     * Put a boolean value for a VM.
     *
     * @param n the node
     * @param k the attribute identifier
     * @param b the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(Node n, String k, boolean b);

    /**
     * Put a String value for a VM.
     *
     * @param n the node
     * @param k the attribute identifier
     * @param s the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(Node n, String k, String s);

    /**
     * Put a double value for a Node.
     *
     * @param n the node
     * @param k the attribute identifier
     * @param d the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(Node n, String k, double d);

    /**
     * Put an integer value for a Node.
     *
     * @param n the node
     * @param k the attribute identifier
     * @param d the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(Node n, String k, int d);

    /**
     * Get an attribute value as a simple Object.
     *
     * @param n the node
     * @param k the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Object get(Node n, String k);

    /**
     * Get an attribute value as a boolean.
     *
     * @param n the node
     * @param k the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Boolean getBoolean(Node n, String k);

    /**
     * Get an attribute value as a string.
     *
     * @param n the node
     * @param k the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    String getString(Node n, String k);

    /**
     * Get an attribute value as a double.
     *
     * @param n the node
     * @param k the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Double getDouble(Node n, String k);

    /**
     * Get an attribute value as an integer.
     *
     * @param n the node
     * @param k the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Integer getInteger(Node n, String k);


    /**
     * Check if an attribute is set for a given element.
     *
     * @param n the node
     * @param k the attribute identifier
     * @return {@code true} iff the attribute is set
     */
    boolean isSet(Node n, String k);

    /**
     * Unset an attribute for a node.
     *
     * @param n the node
     * @param k the attribute identifier
     * @return {@code true} iff a value was removed
     */
    boolean unset(Node n, String k);

    /**
     * Get the nodes having attributes defined.
     *
     * @return a set that may be empty
     */
    Set<Node> getSpecifiedNodes();

    /**
     * Get all the attributes keys that are registered for a node.
     *
     * @param n the node
     * @return a set that may be empty
     */
    Set<String> getKeys(Node n);

    /**
     * Put a value but try to cast into to a supported primitive if possible.
     * First, it tries to cast {@code v} first to a boolean, then to a integer value,
     * finally to a double value. If none of the cast succeeded, the value is let
     * as a string.
     *
     * @param n the node
     * @param k the attribute identifier
     * @param v the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean castAndPut(Node n, String k, String v);

    /**
     * Remove all the attributes.
     */
    void clear();
}
