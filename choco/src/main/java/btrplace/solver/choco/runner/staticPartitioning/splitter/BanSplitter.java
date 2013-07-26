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

import btrplace.model.*;
import btrplace.model.constraint.Ban;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.List;
import java.util.Set;

/**
 * Splitter for {@link btrplace.model.constraint.Ban} constraints.
 * <p/>
 * When the constraint focuses VMs or nodes among different partitions,
 * the constraint is split accordingly.
 *
 * @author Fabien Hermenier
 */
public class BanSplitter implements ConstraintSplitter<Ban> {

    @Override
    public Class<Ban> getKey() {
        return Ban.class;
    }

    @Override
    public boolean split(Ban cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        final SplittableElementSet<Node> nodeIndex = SplittableElementSet.newNodeIndex(cstr.getInvolvedNodes(), nodePosition);
        return SplittableElementSet.newVMIndex(cstr.getInvolvedVMs(), vmsPosition).
                forEachPartition(new IterateProcedure<VM>() {
                    @Override
                    public boolean extract(SplittableElementSet<VM> index, int idx, int from, int to) {
                        if (to != from) {
                            Set<VM> vms = new ElementSubSet<>(index, idx, from, to);
                            Set<Node> ns = nodeIndex.getSubset(idx);
                            if (ns != null && !ns.isEmpty()) {
                                partitions.get(idx).getSatConstraints().add(new Ban(vms, ns));
                            }
                        }
                        return true;
                    }
                });
    }
}
