/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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
import java.util.UUID;

/**
 * Allow to specify attributes related to managed elements.
 *
 * @author Fabien Hermenier
 */
public interface Attributes extends Cloneable {

    /**
     * Put a boolean value.
     *
     * @param e the element identifier
     * @param k the attribute identifier
     * @param b the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(UUID e, String k, boolean b);

    /**
     * Put a String value.
     *
     * @param e the element identifier
     * @param k the attribute identifier
     * @param s the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(UUID e, String k, String s);

    /**
     * Put a long value.
     *
     * @param e the element identifier
     * @param k the attribute identifier
     * @param l the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(UUID e, String k, long l);

    /**
     * Put a double value.
     *
     * @param e the element identifier
     * @param k the attribute identifier
     * @param d the value to set
     * @return {@code true} if a previous value was overridden
     */
    boolean put(UUID e, String k, double d);

    /**
     * Get an attribute value as a boolean.
     *
     * @param e the element identifier
     * @param k the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Boolean getBoolean(UUID e, String k);

    /**
     * Get an attribute value as a long.
     *
     * @param e the element identifier
     * @param k the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Long getLong(UUID e, String k);

    /**
     * Get an attribute value as a string.
     *
     * @param e the element identifier
     * @param k the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    String getString(UUID e, String k);

    /**
     * Get an attribute value as a double.
     *
     * @param e the element identifier
     * @param k the attribute value
     * @return the value if it has been stated. {@code null} otherwise
     */
    Double getDouble(UUID e, String k);

    /**
     * Check if an attribute is set for a given element.
     *
     * @param e the element
     * @param k the attribute identifier
     * @return {@code true} iff the attribute is set
     */
    boolean isSet(UUID e, String k);

    /**
     * Unset an attribute for a given element.
     *
     * @param e the element identifier
     * @param k the attribute identifier
     * @return {@code true} iff a value was removed
     */
    boolean unset(UUID e, String k);

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
    Set<UUID> getElements();

    /**
     * Remove all the attributes.
     */
    void clear();
}
