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
import btrplace.model.constraint.Among;
import btrplace.solver.choco.runner.staticPartitioning.IndexEntry;
import btrplace.solver.choco.runner.staticPartitioning.IndexEntryProcedure;
import btrplace.solver.choco.runner.staticPartitioning.SplittableIndex;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Splitter for {@link btrplace.model.constraint.Among} constraints.
 * <p/>
 * When the constraint focuses VMs among different partitions,
 * the constraint is splitted accordingly.
 * If the nodes groups are also splitted among different partitions,
 * this leads to a un-solvable problem as it is not possible to
 * synchronize the sub-among constraints to make them choose the same nodes group.
 *
 * @author Fabien Hermenier
 */
public class AmongSplitter implements ConstraintSplitter<Among> {

    @Override
    public Class<Among> getKey() {
        return Among.class;
    }

    @Override
    public boolean split(final Among cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, final TIntIntHashMap nodePosition) {

        final boolean c = cstr.isContinuous();
        return SplittableIndex.newVMIndex(cstr.getInvolvedVMs(), vmsPosition).
                forEachIndexEntry(new IndexEntryProcedure<VM>() {
                    @Override
                    public boolean extract(SplittableIndex<VM> index, int idx, int from, int to) {
                        if (to - from >= 2) {
                            IndexEntry<VM> vms = new IndexEntry<>(index, idx, from, to);
                            //Get the servers on the partition

                            //Filter out the other nodes in the original constraint
                            final Collection<Collection<Node>> subParams = new ArrayList<>();
                            for (Collection<Node> ns : cstr.getGroupsOfNodes()) {
                                SplittableIndex<Node> nodeIndex = SplittableIndex.newNodeIndex(ns, nodePosition);
                                nodeIndex.forEachIndexEntry(new IndexEntryProcedure<Node>() {
                                    @Override
                                    public boolean extract(SplittableIndex<Node> index, int key, int from, int to) {
                                        if (from != to) {
                                            subParams.add(new IndexEntry<Node>(index, key, from, to));
                                        }
                                        return true;
                                    }
                                });
                            }
                            partitions.get(idx).getConstraints().add(new Among(vms, subParams));
                        }
                        return true;
                    }
                });

        /*
        Set<VM> vms = new HashSet<>(cstr.getInvolvedVMs());
        List<Set<Node>> parts = new ArrayList<>();
        for (Collection<Node> s : cstr.getGroupsOfNodes()) {
            parts.add(new HashSet<>(s));
        }

        for (Instance i : partitions) {
            Set<VM> vmsIn = Splitters.extractVMsIn(vms, i.getModel().getMapping());

            if (!vmsIn.isEmpty()) {
                Collection<Collection<Node>> subSplit = new ArrayList<>();
                for (Set<Node> s : parts) {
                    Set<Node> in = Splitters.extractNodesIn(s, i.getModel().getMapping());
                    if (!in.isEmpty()) {
                        subSplit.add(in);
                    }
                }
                if (!subSplit.isEmpty()) {
                    i.getConstraints().add(new Among(vmsIn, subSplit, cstr.isContinuous()));
                }
            }
            if (vms.isEmpty()) {
                break;
            }
        }
        return true;  */
    }
}
