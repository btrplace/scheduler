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

import btrplace.model.DefaultModel;
import btrplace.model.Instance;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.constraint.MinMTTR;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit test for {@link btrplace.solver.choco.runner.staticPartitioning.splitter.OfflineSplitter}.
 *
 * @author Fabien Hermenier
 */
public class OfflineSplitterTest {

    @Test
    public void simpleTest() {
        OfflineSplitter splitter = new OfflineSplitter();

        List<Instance> instances = new ArrayList<>();
        Model m0 = new DefaultModel();
        m0.getMapping().addOfflineNode(m0.newNode(0));
        m0.getMapping().addOfflineNode(m0.newNode(1));

        Model m1 = new DefaultModel();
        m1.getMapping().addOfflineNode(m1.newNode(2));
        m1.getMapping().addOfflineNode(m1.newNode(3));

        instances.add(new Instance(m0, new ArrayList<SatConstraint>(), new MinMTTR()));
        instances.add(new Instance(m1, new ArrayList<SatConstraint>(), new MinMTTR()));

        Set<Node> all = new HashSet<>(m0.getNodes());
        all.addAll(m1.getNodes());

        //Only nodes in m0
        Offline oSimple = new Offline(m0.getMapping().getAllNodes());
        Assert.assertTrue(splitter.split(oSimple, instances));
        Assert.assertTrue(instances.get(0).getConstraints().contains(oSimple));
        Assert.assertFalse(instances.get(1).getConstraints().contains(oSimple));

        //All the nodes, test the split
        Offline oAmong = new Offline(all);

        Assert.assertTrue(splitter.split(oAmong, instances));
        Assert.assertTrue(instances.get(0).getConstraints().contains(new Offline(m0.getMapping().getAllNodes())));
        Assert.assertTrue(instances.get(1).getConstraints().contains(new Offline(m1.getMapping().getAllNodes())));
    }
}
