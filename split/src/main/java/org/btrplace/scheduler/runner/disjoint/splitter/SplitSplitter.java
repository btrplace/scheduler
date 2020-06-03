/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint.splitter;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.Instance;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Split;
import org.btrplace.scheduler.runner.disjoint.model.SplittableElementSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Splitter for {@link org.btrplace.model.constraint.Split} constraints.
 * When the constraint focuses VMs among different partitions,
 * the constraint is split.
 * <p>
 * This operation is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class SplitSplitter implements ConstraintSplitter<Split> {

    @Override
    public Class<Split> getKey() {
        return Split.class;
    }

    @Override
    public boolean split(final Split cstr, Instance origin, final List<Instance> partitions, final TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {

        final boolean c = cstr.isContinuous();
        return SplittableElementSet.newVMIndex(cstr.getInvolvedVMs(), vmsPosition).
                forEachPartition((index, idx, from, to) -> {
                    if (to - from >= 2) {
                        //More than 1 VM involved in a split constraint for this partition
                        //if these VMs belong to at least 2 groups, we must post a split constraints
                        //for the VMs on these groups
                        Collection<Collection<VM>> sets = new ArrayList<>();
                        for (Collection<VM> vms : cstr.getSets()) {
                            SplittableElementSet<VM> subSplit = SplittableElementSet.newVMIndex(vms, vmsPosition);
                            Set<VM> s = subSplit.getSubSet(idx);
                            if (!s.isEmpty()) {
                                sets.add(s);
                            }
                        }
                        if (sets.size() > 1) {
                            partitions.get(idx).getSatConstraints().add(new Split(sets, c));
                        }
                    }
                    return true;
                });
    }
}
