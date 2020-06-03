/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint.splitter;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.Instance;
import org.btrplace.model.Node;
import org.btrplace.model.constraint.Quarantine;

import java.util.List;

/**
 * Splitter for {@link org.btrplace.model.constraint.Quarantine} constraints.
 * <p>
 * When the constraint focuses nodes among different partitions,
 * the constraint is split.
 * <p>
 * This operation is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class QuarantineSplitter implements ConstraintSplitter<Quarantine> {

    @Override
    public Class<Quarantine> getKey() {
        return Quarantine.class;
    }

    @Override
    public boolean split(Quarantine cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        Node n = cstr.getInvolvedNodes().iterator().next();
        int i = nodePosition.get(n.id());
        return partitions.get(i).getSatConstraints().add(cstr);
    }
}
