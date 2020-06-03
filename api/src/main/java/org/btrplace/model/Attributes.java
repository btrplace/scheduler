/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

import org.btrplace.Copyable;

import java.util.Set;

/**
 * Allow to specify attributes related to managed elements.
 * Attributes are key/value pairs, where values are Java primitives (integer, double, String, boolean)
 *
 * @author Fabien Hermenier
 */
public interface Attributes extends Copyable<Attributes> {

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
     * @param def the value to return if the attribute does not exists
     * @return the value if it has been stated or {@code def}
     */
    boolean get(Element e, String k, boolean def);

    /**
     * Get an attribute value as a string.
     *
     * @param e the element
     * @param k the attribute value
     * @param def the default value if missing
     * @return the value if it has been stated. {@code null} otherwise
     */
    String get(Element e, String k, String def);

    /**
     * Get an attribute value as a double.
     *
     * @param e the element
     * @param k the attribute value
     * @param def the value to return if the attribute does not exists
     * @return the value if it has been stated or {@code def}
     */
    double get(Element e, String k, double def);

    /**
     * Get an attribute value as an integer.
     *
     * @param e the element
     * @param k the attribute value
     * @param def the value to return if the attribute does not exists
     * @return the value if it has been stated or {@code def}
     */
    int get(Element e, String k, int def);

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
