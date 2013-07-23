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
import btrplace.model.VM;
import btrplace.model.constraint.Fence;
import btrplace.solver.choco.runner.staticPartitioning.IndexEntry;
import btrplace.solver.choco.runner.staticPartitioning.IndexEntryProcedure;
import btrplace.solver.choco.runner.staticPartitioning.SplittableIndex;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.List;
import java.util.Set;

/**
 * Splitter for {@link btrplace.model.constraint.Fence} constraints.
 * <p/>
 * When the constraint focuses VMs or nodes among different partitions,
 * the constraint is splitted accordingly.
 *
 * @author Fabien Hermenier
 */
public class FenceSplitter implements ConstraintSplitter<Fence> {

    @Override
    public Class<Fence> getKey() {
        return Fence.class;
    }

    @Override
    public boolean split(Fence cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        final SplittableIndex<Node> nodeIndex = SplittableIndex.newNodeIndex(cstr.getInvolvedNodes(), nodePosition);
        return SplittableIndex.newVMIndex(cstr.getInvolvedVMs(), vmsPosition).
                forEachIndexEntry(new IndexEntryProcedure<VM>() {
                    @Override
                    public boolean extract(SplittableIndex<VM> index, int idx, int from, int to) {
                        if (to != from) {
                            Set<VM> vms = new IndexEntry<>(index, idx, from, to);
                            Set<Node> ns = nodeIndex.makeIndexEntry(idx);
                            if (!ns.isEmpty()) {
                                partitions.get(idx).getConstraints().add(new Fence(vms, ns));
                            } else {
                                return false;
                            }
                        }
                        return true;
                    }
                });
    }
}
