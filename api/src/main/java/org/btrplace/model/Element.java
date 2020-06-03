/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

/**
 * An element managed by BtrPlace.
 *
 * @author Fabien Hermenier
 */
@FunctionalInterface
public interface Element {

    /**
     * The element identifier.
     * Unique among every elements having the same type
     *
     * @return an integer
     */
    int id();

}
