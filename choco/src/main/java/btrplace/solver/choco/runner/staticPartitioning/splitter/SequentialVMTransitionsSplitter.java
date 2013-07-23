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
import btrplace.model.constraint.SequentialVMTransitions;
import btrplace.solver.choco.runner.staticPartitioning.IndexEntryProcedure;
import btrplace.solver.choco.runner.staticPartitioning.SplittableIndex;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.List;

/**
 * Splitter for {@link btrplace.model.constraint.SequentialVMTransitions} constraints.
 * <p/>
 * The splitting process is supported iff all the VMs belong to the same partitions.
 * If not, a {@link UnsupportedOperationException} is thrown.
 * <p/>
 * This operation is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class SequentialVMTransitionsSplitter implements ConstraintSplitter<SequentialVMTransitions> {

    @Override
    public Class<SequentialVMTransitions> getKey() {
        return SequentialVMTransitions.class;
    }

    @Override
    public boolean split(SequentialVMTransitions cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        final List<VM> seq = cstr.getInvolvedVMs();
        return SplittableIndex.newVMIndex(seq, vmsPosition).
                forEachIndexEntry(new IndexEntryProcedure<VM>() {

                    private boolean first = true;

                    @Override
                    public boolean extract(SplittableIndex<VM> index, int idx, int from, int to) {
                        int size = to - from;
                        if (!first) {
                            throw new UnsupportedOperationException("Splitting a SequentialVMTransitions over multiple partitions is not supported");
                        }
                        if (size == seq.size()) {
                            partitions.get(idx).getConstraints().add(new SequentialVMTransitions(seq));
                            first = false;
                        }
                        return true;
                    }
                });
    }
}
