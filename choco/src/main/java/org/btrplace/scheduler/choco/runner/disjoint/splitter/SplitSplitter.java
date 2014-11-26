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
import org.btrplace.model.IterateProcedure;
import org.btrplace.model.SplittableElementSet;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Split;

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
                forEachPartition(new IterateProcedure<VM>() {
                    @Override
                    public boolean extract(SplittableElementSet<VM> index, int idx, int from, int to) {
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
                    }
                });
    }
}
