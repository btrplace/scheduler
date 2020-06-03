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
import org.btrplace.model.constraint.Fence;
import org.btrplace.scheduler.runner.disjoint.model.SplittableElementSet;

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
