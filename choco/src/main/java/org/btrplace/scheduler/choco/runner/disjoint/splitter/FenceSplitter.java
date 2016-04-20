/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
import org.btrplace.model.Instance;
import org.btrplace.model.Node;
import org.btrplace.model.SplittableElementSet;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Fence;

import java.util.List;
import java.util.Set;

/**
 * Splitter for {@link org.btrplace.model.constraint.Fence} constraints.
 * <p>
 * When the constraint focuses VMs or nodes among different partitions,
 * the constraint is split accordingly.
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
        final SplittableElementSet<Node> nodeIndex = SplittableElementSet.newNodeIndex(cstr.getInvolvedNodes(), nodePosition);

        VM v = cstr.getInvolvedVMs().iterator().next();
        int p = vmsPosition.get(v.id());

        Set<Node> ns = nodeIndex.getSubSet(p);
        if (!ns.isEmpty()) {
            return partitions.get(p).getSatConstraints().add(new Fence(v, ns));
        }
        return true;
    }
}
