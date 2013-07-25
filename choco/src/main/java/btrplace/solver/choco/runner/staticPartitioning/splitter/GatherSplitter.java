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
import btrplace.model.constraint.Gather;
import btrplace.solver.choco.runner.staticPartitioning.IndexEntry;
import btrplace.solver.choco.runner.staticPartitioning.IndexEntryProcedure;
import btrplace.solver.choco.runner.staticPartitioning.SplittableIndex;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.List;

/**
 * Splitter for {@link btrplace.model.constraint.Gather} constraints.
 * <p/>
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
        return SplittableIndex.newVMIndex(cstr.getInvolvedVMs(), vmsPosition).
                forEachIndexEntry(new IndexEntryProcedure<VM>() {

                    private boolean first = true;

                    @Override
                    public boolean extract(SplittableIndex<VM> index, int idx, int from, int to) {
                        if (!first) {
                            return false;
                        }
                        if (to - from >= 2) {
                            partitions.get(idx).getSatConstraints().add(new Gather(new IndexEntry<>(index, idx, from, to), c));
                            first = false;
                        }
                        return true;
                    }
                });
    }
}
