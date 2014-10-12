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

package btrplace.solver.choco.runner.disjoint.splitter;

import btrplace.model.DefaultModel;
import btrplace.model.Instance;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.constraint.MinMTTR;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.SatConstraint;
import gnu.trove.map.hash.TIntIntHashMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit test for {@link btrplace.solver.choco.runner.disjoint.splitter.OfflineSplitter}.
 *
 * @author Fabien Hermenier
 */
public class OfflineSplitterTest {

    @Test
    public void simpleTest() {
        OfflineSplitter splitter = new OfflineSplitter();

        List<Instance> instances = new ArrayList<>();
        Model m0 = new DefaultModel();
        Node n = m0.newNode();
        m0.getMapping().addOfflineNode(n);
        m0.getMapping().addOfflineNode(m0.newNode(1));

        Model m1 = new DefaultModel();

        m1.getMapping().addOfflineNode(m1.newNode(2));
        m1.getMapping().addOfflineNode(m1.newNode(3));

        instances.add(new Instance(m0, new ArrayList<SatConstraint>(), new MinMTTR()));
        instances.add(new Instance(m1, new ArrayList<SatConstraint>(), new MinMTTR()));

        Set<Node> all = new HashSet<>(m0.getMapping().getAllNodes());
        all.addAll(m1.getMapping().getAllNodes());

        TIntIntHashMap nodeIndex = Instances.makeNodeIndex(instances);

        //Only nodes in m0
        Offline oSimple = new Offline(n);
        Assert.assertTrue(splitter.split(oSimple, null, instances, new TIntIntHashMap(), nodeIndex));
        Assert.assertTrue(instances.get(0).getSatConstraints().contains(oSimple));
        Assert.assertFalse(instances.get(1).getSatConstraints().contains(oSimple));
    }
}
