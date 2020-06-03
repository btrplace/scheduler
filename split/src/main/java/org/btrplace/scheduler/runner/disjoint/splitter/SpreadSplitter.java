/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint.splitter;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.Instance;
import org.btrplace.model.constraint.Spread;
import org.btrplace.scheduler.runner.disjoint.model.ElementSubSet;
import org.btrplace.scheduler.runner.disjoint.model.SplittableElementSet;

import java.util.List;

/**
 * Splitter for {@link Spread} constraints.
 * When the constraint focuses VMs among different partitions,
 * the constraint is split.
 * <p>
 * This operation is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class SpreadSplitter implements ConstraintSplitter<Spread> {

    @Override
    public Class<Spread> getKey() {
        return Spread.class;
    }

    @Override
    public boolean split(final Spread cstr, Instance origin, final List<Instance> partitions, final TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        final boolean c = cstr.isContinuous();
        return SplittableElementSet.newVMIndex(cstr.getInvolvedVMs(), vmsPosition).
                forEachPartition((index, idx, from, to) -> {
                    if (to - from >= 2) {
                        partitions.get(idx).getSatConstraints().add(new Spread(new ElementSubSet<>(index, idx, from, to), c));
                    }
                    return true;
                });
    }
}
