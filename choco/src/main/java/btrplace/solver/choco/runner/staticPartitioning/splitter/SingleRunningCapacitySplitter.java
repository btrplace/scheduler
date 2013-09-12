/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

package btrplace.solver.choco.runner.staticPartitioning.splitter;

import btrplace.model.*;
import btrplace.model.constraint.SingleRunningCapacity;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.List;

/**
 * Splitter for {@link btrplace.model.constraint.SingleRunningCapacity} constraints.
 * <p/>
 * When the constraint focuses nodes among different partitions,
 * the constraint is split.
 * <p/>
 * This operation is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class SingleRunningCapacitySplitter implements ConstraintSplitter<SingleRunningCapacity> {

    @Override
    public Class<SingleRunningCapacity> getKey() {
        return SingleRunningCapacity.class;
    }

    @Override
    public boolean split(SingleRunningCapacity cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        final boolean c = cstr.isContinuous();
        final int qty = cstr.getAmount();
        return SplittableElementSet.newNodeIndex(cstr.getInvolvedNodes(), nodePosition).
                forEachPartition(new IterateProcedure<Node>() {
                    @Override
                    public boolean extract(SplittableElementSet<Node> index, int idx, int from, int to) {
                        if (to != from) {
                            partitions.get(idx).getSatConstraints().add(new SingleRunningCapacity(new ElementSubSet<>(index, idx, from, to), qty, c));
                        }
                        return true;
                    }
                });
    }
}
