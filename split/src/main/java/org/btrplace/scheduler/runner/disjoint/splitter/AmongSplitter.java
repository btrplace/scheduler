/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint.splitter;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.Instance;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Among;
import org.btrplace.scheduler.runner.disjoint.model.ElementSubSet;
import org.btrplace.scheduler.runner.disjoint.model.SplittableElementSet;

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
                forEachPartition((index, idx, from, to) -> {
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
                });
    }
}
