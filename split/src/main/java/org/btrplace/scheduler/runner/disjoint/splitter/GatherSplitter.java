/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint.splitter;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.Instance;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Gather;
import org.btrplace.scheduler.runner.disjoint.model.ElementSubSet;
import org.btrplace.scheduler.runner.disjoint.model.IterateProcedure;
import org.btrplace.scheduler.runner.disjoint.model.SplittableElementSet;

import java.util.List;

/**
 * Splitter for {@link org.btrplace.model.constraint.Gather} constraints.
 * <p>
 * When the constraint focuses VMs among different partitions,
 * then it is sure the problem has no solutions.
 *
 * @author Fabien Hermenier
 */
public class GatherSplitter implements ConstraintSplitter<Gather> {

    @Override
    public Class<Gather> getKey() {
        return Gather.class;
    }

    @Override
    public boolean split(Gather cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        final boolean c = cstr.isContinuous();
        return SplittableElementSet.newVMIndex(cstr.getInvolvedVMs(), vmsPosition).
                forEachPartition(new IterateProcedure<VM>() {

                    private boolean first = true;

                    @Override
                    public boolean extract(SplittableElementSet<VM> index, int idx, int from, int to) {
                        if (!first) {
                            return false;
                        }
                        if (to - from >= 2) {
                            partitions.get(idx).getSatConstraints().add(new Gather(new ElementSubSet<>(index, idx, from, to), c));
                            first = false;
                        }
                        return true;
                    }
                });
    }
}
