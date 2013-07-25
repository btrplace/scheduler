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
import btrplace.model.Node;
import btrplace.model.constraint.Offline;
import btrplace.solver.choco.runner.staticPartitioning.IndexEntry;
import btrplace.solver.choco.runner.staticPartitioning.IndexEntryProcedure;
import btrplace.solver.choco.runner.staticPartitioning.SplittableIndex;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.List;

/**
 * Splitter for {@link btrplace.model.constraint.Offline} constraints.
 * <p/>
 * When the constraint focuses nodes among different partitions,
 * the constraint is split.
 * <p/>
 * This operation is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class OfflineSplitter implements ConstraintSplitter<Offline> {

    @Override
    public Class<Offline> getKey() {
        return Offline.class;
    }

    @Override
    public boolean split(Offline cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        return SplittableIndex.newNodeIndex(cstr.getInvolvedNodes(), nodePosition).
                forEachIndexEntry(new IndexEntryProcedure<Node>() {
                    @Override
                    public boolean extract(SplittableIndex<Node> index, int idx, int from, int to) {
                        if (to != from) {
                            partitions.get(idx).getConstraints().add(new Offline(new IndexEntry<>(index, idx, from, to)));
                        }
                        return true;
                    }
                });
    }
}