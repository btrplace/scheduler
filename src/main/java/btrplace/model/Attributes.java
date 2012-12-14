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
     * declare an attribute for a given element.
     * Equivalent to {@code set(e, k, Boolean.TRUE)}
     *
     * @param e the element
     * @param k the attribute identifier
     * @return the previous attribute value if the attribute was stated earlier, {@code null} otherwise
     */
    Object set(UUID e, String k);

    /**
     * set an attribute value for a given element.
     *
     * @param e the element
     * @param k the attribute identifier
     * @param v the attribute value
     * @return the previous attribute value if the attribute was stated earlier, {@code null} otherwise
     */
    Object set(UUID e, String k, Object v);

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
     * @return the previous value if the attribute was already stated, @{@code null} otherwise
     */
    Object unset(UUID e, String k);

    /**
     * Get all the declared attribute for a given element.
     *
     * @param e the element identifier
     * @return a set of attribute identifier that may be empty
     */
    Set<String> get(UUID e);

    /**
     * Get the value associated to an attribute.
     *
     * @param e the element identifier
     * @param k the attribute identifier
     * @return the attribute value iff it was stated earlier, {@code null} otherwise
     */
    Object get(UUID e, String k);

    /**
     * Clone the attributes.
     *
     * @return a new set of attributes
     */
    Attributes clone();

    /**
     * Remove all the attributes.
     */
    void clear();
}
