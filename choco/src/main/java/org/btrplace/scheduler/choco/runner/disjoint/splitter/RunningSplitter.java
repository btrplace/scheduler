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
