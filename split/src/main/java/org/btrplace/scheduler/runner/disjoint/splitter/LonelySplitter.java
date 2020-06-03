/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint.splitter;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.Instance;
import org.btrplace.model.constraint.Lonely;
import org.btrplace.scheduler.runner.disjoint.model.ElementSubSet;
import org.btrplace.scheduler.runner.disjoint.model.SplittableElementSet;

import java.util.List;

/**
 * Splitter for {@link org.btrplace.model.constraint.Lonely} constraints.
 * When the constraint focuses VMs among different partitions,
 * the constraint is split.
 * <p>
 * This operation is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class LonelySplitter implements ConstraintSplitter<Lonely> {


    @Override
    public Class<Lonely> getKey() {
        return Lonely.class;
    }

    @Override
    public boolean split(Lonely cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        final boolean c = cstr.isContinuous();
        return SplittableElementSet.newVMIndex(cstr.getInvolvedVMs(), vmsPosition).
                forEachPartition((index, idx, from, to) -> {
                    if (to != from) {
                        partitions.get(idx).getSatConstraints().add(new Lonely(new ElementSubSet<>(index, idx, from, to), c));
                    }
                    return true;
                });
    }
}
