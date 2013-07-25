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
import btrplace.model.constraint.Running;
import btrplace.solver.choco.runner.staticPartitioning.IndexEntry;
import btrplace.solver.choco.runner.staticPartitioning.IndexEntryProcedure;
import btrplace.solver.choco.runner.staticPartitioning.SplittableIndex;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.List;

/**
 * Splitter for the {@link btrplace.model.constraint.Running} constraints.
 * When the constraint focuses VMs among different partitions,
 * the constraint is split.
 * <p/>
 * The split process is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class RunningSplitter implements ConstraintSplitter<Running> {

    @Override
    public Class<Running> getKey() {
        return Running.class;
    }

    @Override
    public boolean split(Running cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        return SplittableIndex.newVMIndex(cstr.getInvolvedVMs(), vmsPosition).
                forEachIndexEntry(new IndexEntryProcedure<VM>() {
                    @Override
                    public boolean extract(SplittableIndex<VM> index, int idx, int from, int to) {
                        if (to != from) {
                            partitions.get(idx).getSatConstraints().add(new Running(new IndexEntry<>(index, idx, from, to)));
                        }
                        return true;
                    }
                });
    }
}
