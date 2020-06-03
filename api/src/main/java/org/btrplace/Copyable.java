/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace;

/**
 * Copyable is a safer alternative to clone.
 *
 * @author Fabien Hermenier
 */
@FunctionalInterface
public interface Copyable<T> {

    /**
     * Make a deep copy of the object.
     *
     * @return a deep copy
     */
    T copy();
}
