/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint.splitter;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.Instance;
import org.btrplace.model.Node;
import org.btrplace.model.constraint.MaxOnline;
import org.btrplace.scheduler.runner.disjoint.model.ElementSubSet;
import org.btrplace.scheduler.runner.disjoint.model.IterateProcedure;
import org.btrplace.scheduler.runner.disjoint.model.SplittableElementSet;

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
