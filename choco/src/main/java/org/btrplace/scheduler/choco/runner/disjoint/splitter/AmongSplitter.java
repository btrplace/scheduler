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
import org.btrplace.model.*;
import org.btrplace.model.constraint.Among;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Splitter for {@link org.btrplace.model.constraint.Among} constraints.
 * <p>
 * When the constraint focuses VMs among different partitions,
 * the constraint is split accordingly.
 * If the nodes groups are also split among different partitions,
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
        return SplittableElementSet.newVMIndex(cstr.getInvolvedVMs(), vmsPosition).
                forEachPartition(new IterateProcedure<VM>() {
                    @Override
                    public boolean extract(SplittableElementSet<VM> index, int idx, int from, int to) {
                        if (to - from >= 2) {
                            ElementSubSet<VM> vms = new ElementSubSet<>(index, idx, from, to);
                            //Get the servers on the partition

                            //Filter out the other nodes in the original constraint
                            final Collection<Collection<Node>> subParams = new ArrayList<>();
                            for (Collection<Node> ns : cstr.getGroupsOfNodes()) {
                                SplittableElementSet<Node> nodeIndex = SplittableElementSet.newNodeIndex(ns, nodePosition);
                                Set<Node> s = nodeIndex.getSubSet(idx);
                                if (s != null && !s.isEmpty()) {
                                    subParams.add(s);
                                }
                            }
                            partitions.get(idx).getSatConstraints().add(new Among(vms, subParams, c));
                        }
                        return true;
                    }
                });
    }
}
