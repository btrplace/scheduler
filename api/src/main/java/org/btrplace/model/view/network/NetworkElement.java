/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.view.network;

/**
 * Model a network element defined by an identifier
 * and a bandwidth capacity.
 *
 * @author Vincent Kherbache
 */
public interface NetworkElement {

    /**
     * Get the element identifier.
     *
     * @return a unique number
     */
    int id();

    /**
     * Get the bandwidth capacity.
     *
     * @return a positive value
     */
    int getCapacity();
}
