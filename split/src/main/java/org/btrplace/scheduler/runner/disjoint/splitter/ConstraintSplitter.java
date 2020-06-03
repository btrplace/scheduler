/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint.splitter;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.Instance;
import org.btrplace.model.constraint.Constraint;

import java.util.List;

/**
 * Interface to specify a method that makes a constraint
 * compatible with multiple partitions.
 * <p>
 * In practice, the splitter ensures the given constraint
 * does not spread over multiple partitions.
 * This may require to split the constraint.
 *
 * @author Fabien Hermenier
 */
public interface ConstraintSplitter<C extends Constraint> {

    /**
     * Get the class of the Constraint associated to the splitter.
     *
     * @return a Class derived from {@link org.btrplace.model.constraint.Constraint}
     */
    Class<C> getKey();

    /**
     * Ensure a given constraint fit into a single partition.
     * If necessary, the constraint may have be split.
     * <b>this call inserts the constrain (or its subdivisions) inside their respective instances</b>
     *
     * @param cstr         the model constraint
     * @param origin       the original instance to split
     * @param partitions   the possible partitions  @return {@code false} iff this leads to a problem without solutions.
     * @param vmsPosition  the partition associated to each VM
     * @param nodePosition the partition associated to each node
     * @return {@code true} iff the split was successful. {@code false} otherwise
     */
    boolean split(C cstr, Instance origin, List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition);

}
