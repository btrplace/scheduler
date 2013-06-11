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

import btrplace.model.Instance;
import btrplace.model.Mapping;
import btrplace.model.Node;
import btrplace.model.constraint.Offline;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class OfflineSplitter implements ConstraintSplitter<Offline> {

    public OfflineSplitter() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Class<Offline> getKey() {
        return Offline.class;
    }

    @Override
    public boolean split(Offline cstr, List<Instance> partitions) {
        Set<Node> nodes = new HashSet<>(cstr.getInvolvedNodes());
        for (Instance i : partitions) {
            Mapping m = i.getModel().getMapping();
            Set<Node> all = m.getAllNodes();
            Set<Node> in = Splitters.extractInside(nodes, all);
            if (!in.isEmpty()) {
                i.getConstraints().add(new Offline(in));
            }
            if (nodes.isEmpty()) {
                break;
            }
        }
        return true;
    }
}
