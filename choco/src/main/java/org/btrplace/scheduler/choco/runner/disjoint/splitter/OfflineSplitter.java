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
import org.btrplace.model.Node;
import org.btrplace.model.constraint.Offline;

import java.util.List;

/**
 * Splitter for {@link org.btrplace.model.constraint.Offline} constraints.
 * <p>
 * When the constraint focuses nodes among different partitions,
 * the constraint is split.
 * <p>
 * This operation is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class OfflineSplitter implements ConstraintSplitter<Offline> {

    @Override
    public Class<Offline> getKey() {
        return Offline.class;
    }

    @Override
    public boolean split(Offline cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        Node n = cstr.getInvolvedNodes().iterator().next();
        int i = nodePosition.get(n.id());
        return partitions.get(i).getSatConstraints().add(cstr);
    }
}
