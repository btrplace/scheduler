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
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.SingleResourceCapacity;
import gnu.trove.map.hash.TIntIntHashMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link SingleResourceCapacitySplitter}.
 *
 * @author Fabien Hermenier
 */
public class SingleResourceCapacitySplitterTest {

    @Test
    public void simpleTest() {
        SingleResourceCapacitySplitter splitter = new SingleResourceCapacitySplitter();

        List<Instance> instances = new ArrayList<>();
        Model m0 = new DefaultModel();
        m0.getMapping().addReadyVM(m0.newVM(1));
        Node n1 = m0.newNode();
        m0.getMapping().addOnlineNode(n1);
        m0.getMapping().addRunningVM(m0.newVM(2), n1);
        Model m1 = new DefaultModel();
        m1.getMapping().addReadyVM(m1.newVM(3));
        Node n2 = m1.newNode(2);
        m1.getMapping().addOnlineNode(n2);
        m1.getMapping().addSleepingVM(m1.newVM(4), n2);
        m1.getMapping().addRunningVM(m1.newVM(5), n2);


        instances.add(new Instance(m0, new ArrayList<SatConstraint>(), new MinMTTR()));
        instances.add(new Instance(m1, new ArrayList<SatConstraint>(), new MinMTTR()));

        Set<Node> all = new HashSet<>(m0.getMapping().getAllNodes());
        all.addAll(m1.getMapping().getAllNodes());


        //Only VMs in m0
        SingleResourceCapacity single = new SingleResourceCapacity(m0.getMapping().getAllNodes(), "foo", 3);
        Assert.assertTrue(splitter.split(single, null, instances, new TIntIntHashMap(), new TIntIntHashMap()));
        Assert.assertTrue(instances.get(0).getConstraints().contains(single));
        Assert.assertFalse(instances.get(1).getConstraints().contains(single));

        //All the VMs, test the split
        SingleResourceCapacity among = new SingleResourceCapacity(all, "foo", 2);

        Assert.assertTrue(splitter.split(among, null, instances, new TIntIntHashMap(), new TIntIntHashMap()));
        Assert.assertTrue(instances.get(0).getConstraints().contains(new SingleResourceCapacity(m0.getMapping().getAllNodes(), "foo", 2)));
        Assert.assertTrue(instances.get(1).getConstraints().contains(new SingleResourceCapacity(m1.getMapping().getAllNodes(), "foo", 2)));
    }
}
