/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint.model;

import org.btrplace.model.Element;

/**
 * A procedure to use on a set of contiguous elements that
 * belong to the same partition.
 *
 * @author Fabien Hermenier
 */
@FunctionalInterface
public interface IterateProcedure<E extends Element> {

    /**
     * The method to execute.
     *
     * @param index the splittable set to rely on
     * @param key   the partition key
     * @param from  the value lower bound
     * @param to    the value upper bound (exclusive)
     * @return {@code true} to continue
     */
    boolean extract(SplittableElementSet<E> index, int key, int from, int to);
}
