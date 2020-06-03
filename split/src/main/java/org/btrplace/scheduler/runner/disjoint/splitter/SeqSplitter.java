/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint.splitter;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.Instance;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Seq;
import org.btrplace.scheduler.runner.disjoint.model.IterateProcedure;
import org.btrplace.scheduler.runner.disjoint.model.SplittableElementSet;

import java.util.List;

/**
 * Splitter for {@link org.btrplace.model.constraint.Seq} constraints.
 * <p>
 * The splitting process is supported iff all the VMs belong to the same partitions.
 * If not, a {@link UnsupportedOperationException} is thrown.
 * <p>
 * This operation is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class SeqSplitter implements ConstraintSplitter<Seq> {

    @Override
    public Class<Seq> getKey() {
        return Seq.class;
    }

    @Override
    public boolean split(Seq cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        final List<VM> seq = cstr.getInvolvedVMs();
        return SplittableElementSet.newVMIndex(seq, vmsPosition).
                forEachPartition(new IterateProcedure<VM>() {

                    private boolean first = true;

                    @Override
                    public boolean extract(SplittableElementSet<VM> index, int idx, int from, int to) {
                        int size = to - from;
                        if (!first) {
                            throw new UnsupportedOperationException("Splitting a Seq over multiple partitions is not supported");
                        }
                        if (size == seq.size()) {
                            partitions.get(idx).getSatConstraints().add(new Seq(seq));
                            first = false;
                        }
                        return true;
                    }
                });
    }
}
