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

package org.btrplace.model.view.network;

import org.btrplace.Copyable;
import org.btrplace.model.PhysicalElement;

/**
 * Interface to specify a builder to create links.
 * Each created link is guarantee for being unique.
 *
 * @author Vincent Kherbache
 */
public interface LinkBuilder extends Copyable<LinkBuilder> {

    /**
     * Generate a new Link with unlimited BW.
     *
     * @return {@code null} if no identifiers are available for the Link.
     */
    Link newLink(Switch sw, PhysicalElement pe);

    /**
     * Generate a new Link.
     *
     * @param id    the identifier to use for the Link.
     * @return      a Link or {@code null} if the identifier is already used.
     */
    Link newLink(int id, int capacity, Switch sw, PhysicalElement pe);

    /**
     * Generate a new Link.
     *
     * @param capacity the maximal BW capacity of the Link
     * @return {@code null} if no identifiers are available for the Link.
     */
    Link newLink(int capacity, Switch sw, PhysicalElement pe);

    /**
     * Check if a given Link has been defined fot this network view.
     *
     * @param   l the Link to check.
     * @return  {@code true} if the Link already exists.
     */
    boolean contains(Link l);
}
