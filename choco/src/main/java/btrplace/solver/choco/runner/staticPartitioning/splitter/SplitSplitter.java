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

import btrplace.model.Instance;
import btrplace.model.VM;
import btrplace.model.constraint.Split;
import btrplace.solver.choco.runner.staticPartitioning.IndexEntry;
import btrplace.solver.choco.runner.staticPartitioning.IndexEntryProcedure;
import btrplace.solver.choco.runner.staticPartitioning.SplittableIndex;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Splitter for {@link btrplace.model.constraint.Split} constraints.
 * When the constraint focuses VMs among different partitions,
 * the constraint is split.
 * <p/>
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
        return SplittableIndex.newVMIndex(cstr.getInvolvedVMs(), vmsPosition).
                forEachIndexEntry(new IndexEntryProcedure<VM>() {
                    @Override
                    public boolean extract(SplittableIndex<VM> index, int idx, int from, int to) {
                        if (to - from >= 2) {
                            //More than 1 VM involved in a split constraint for this partition
                            //if these VMs belong to at least 2 groups, we must post a split constraints
                            //for the VMs on these groups
                            Collection<Collection<VM>> sets = new ArrayList<>();
                            for (Collection<VM> vms : cstr.getSets()) {
                                SplittableIndex<VM> subSplit = SplittableIndex.newVMIndex(vms, vmsPosition);
                                IndexEntry<VM> s = subSplit.makeIndexEntry(idx);
                                if (!s.isEmpty()) {
                                    sets.add(s);
                                }
                            }
                            if (sets.size() > 1) {
                                partitions.get(idx).getConstraints().add(new Split(sets, c));
                            }
                        }
                        return true;
                    }
                });
    }
}
