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

package org.btrplace.scheduler.choco.runner.disjoint.splitter;

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
