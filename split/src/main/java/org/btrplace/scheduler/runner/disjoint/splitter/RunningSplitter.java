/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint.splitter;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.Instance;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Running;

import java.util.List;

/**
 * Splitter for the {@link org.btrplace.model.constraint.Running} constraints.
 * When the constraint focuses VMs among different partitions,
 * the constraint is split.
 * <p>
 * The split process is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class RunningSplitter implements ConstraintSplitter<Running> {

    @Override
    public Class<Running> getKey() {
        return Running.class;
    }

    @Override
    public boolean split(Running cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        VM v = cstr.getInvolvedVMs().iterator().next();
        int i = vmsPosition.get(v.id());
        return partitions.get(i).getSatConstraints().add(cstr);
    }
}
