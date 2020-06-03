/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
     * @param sw the switch
     * @param pe the element to connect to the switch
     * @return {@code null} if no identifiers are available for the Link.
     */
    Link newLink(Switch sw, PhysicalElement pe);

    /**
     * Generate a new Link.
     *
     * @param id the identifier to use for the Link.
     * @param capacity the bandwidth capacity
     * @param sw the switch
     * @param pe the element to connect to the switch
     * @return a Link or {@code null} if the identifier is already used.
     */
    Link newLink(int id, int capacity, Switch sw, PhysicalElement pe);

    /**
     * Generate a new Link.
     *
     * @param capacity the maximal BW capacity of the Link
     * @param sw the switch
     * @param pe the element to connect to the switch
     * @return {@code null} if no identifiers are available for the Link.
     */
    Link newLink(int capacity, Switch sw, PhysicalElement pe);

    /**
     * Check if a given Link has been defined fot this network view.
     *
     * @param l the Link to check.
     * @return {@code true} if the Link already exists.
     */
    boolean contains(Link l);
}
