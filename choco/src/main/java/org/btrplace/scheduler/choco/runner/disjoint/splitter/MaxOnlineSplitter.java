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
import org.btrplace.model.*;
import org.btrplace.model.constraint.MaxOnline;

import java.util.List;

/**
 * Splitter for the {@link MaxOnline} constraint.
 * The constraint can be split iff all the nodes belong to the same partition.
 * Otherwise, this would lead to a subjective splitting.
 *
 * @author Fabien Hermenier
 */
public class MaxOnlineSplitter implements ConstraintSplitter<MaxOnline> {

    @Override
    public Class<MaxOnline> getKey() {
        return MaxOnline.class;
    }

    @Override
    public boolean split(MaxOnline cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        final boolean c = cstr.isContinuous();
        final int q = cstr.getAmount();
        return SplittableElementSet.newNodeIndex(cstr.getInvolvedNodes(), nodePosition).
                forEachPartition(new IterateProcedure<Node>() {

                    private boolean first = true;

                    @Override
                    public boolean extract(SplittableElementSet<Node> index, int idx, int from, int to) {
                        if (!first) {
                            return false;
                        }
                        if (to - from >= 1) {
                            partitions.get(idx).getSatConstraints().add(new MaxOnline(new ElementSubSet<>(index, idx, from, to), q, c));
                            first = false;
                        }
                        return true;
                    }
                });

    }
}
