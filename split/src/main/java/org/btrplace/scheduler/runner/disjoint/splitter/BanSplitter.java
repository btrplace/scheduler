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
import org.btrplace.model.constraint.Ban;
import org.btrplace.scheduler.runner.disjoint.model.SplittableElementSet;

import java.util.List;

/**
 * Splitter for {@link org.btrplace.model.constraint.Ban} constraints.
 * <p>
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
        VM v = cstr.getInvolvedVMs().iterator().next();
        int p = vmsPosition.get(v.id());
        return partitions.get(p).getSatConstraints().add(new Ban(v, nodeIndex.getSubSet(p)));
    }
}
