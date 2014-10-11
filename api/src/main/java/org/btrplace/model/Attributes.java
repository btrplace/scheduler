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

import java.util.Set;

/**
 * Allow to specify attributes related to managed elements.
 * Attributes are key/value pairs, where values are Java primitives (integer, double, String, boolean)
 *
 * @author Fabien Hermenier
 */
public interface Attributes extends Cloneable {

    /**
     * Put a boolean value.
     *
     * @param e the element
     * @param k the attribute identifier
     * @param b the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(Element e, String k, boolean b);

    /**
     * Put a String value.
     *
     * @param e the element
     * @param k the attribute identifier
     * @param s the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(Element e, String k, String s);

    /**
     * Put a double value.
     *
     * @param e the element
     * @param k the attribute identifier
     * @param d the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(Element e, String k, double d);

    /**
     * Put an integer value.
     *
     * @param e the element
     * @param k the attribute identifier
     * @param d the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(Element e, String k, int d);

    /**
     * Get an attribute value as a simple Object.
     *
     * @param e the element
     * @param k the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Object get(Element e, String k);

    /**
     * Get an attribute value as a boolean.
     *
     * @param e the element
     * @param k the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Boolean getBoolean(Element e, String k);

    /**
     * Get an attribute value as a string.
     *
     * @param e the element
     * @param k the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    String getString(Element e, String k);

    /**
     * Get an attribute value as a double.
     *
     * @param e the element
     * @param k the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Double getDouble(Element e, String k);

    /**
     * Get an attribute value as an integer.
     *
     * @param e the element
     * @param k the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Integer getInteger(Element e, String k);

    /**
     * Check if an attribute is set for a given element.
     *
     * @param e the element
     * @param k the attribute identifier
     * @return {@code true} iff the attribute is set
     */
    boolean isSet(Element e, String k);

    /**
     * Unset an attribute for a given element.
     *
     * @param e the element
     * @param k the attribute identifier
     * @return {@code true} iff a value was removed
     */
    boolean unset(Element e, String k);

    /**
     * Clone the attributes.
     *
     * @return a new set of attributes
     */
    Attributes clone();

    /**
     * Get the elements having attributes defined.
     *
     * @return a set that may be empty
     */
    Set<Element> getDefined();

    /**
     * Get all the attributes keys that are registered.
     *
     * @param e the element
     * @return a set that may be empty
     */
    Set<String> getKeys(Element e);

    /**
     * Put a value but try to cast into to a supported primitive if possible.
     * First, it tries to cast {@code v} first to a boolean, then to a integer value,
     * finally to a double value. If none of the cast succeeded, the value is let
     * as a string.
     *
     * @param e the element
     * @param k the attribute identifier
     * @param v the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean castAndPut(Element e, String k, String v);

    /**
     * Remove all the attributes.
     */
    void clear();

    /**
     * Remove all the attributes of a given element.
     *
     * @param e the element
     */
    void clear(Element e);
}
