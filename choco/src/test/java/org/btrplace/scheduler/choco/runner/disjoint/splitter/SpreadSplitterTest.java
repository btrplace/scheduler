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
import org.btrplace.model.*;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.Spread;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link SpreadSplitter}.
 *
 * @author Fabien Hermenier
 */
public class SpreadSplitterTest {

    @Test
    public void simpleTest() {
        SpreadSplitter splitter = new SpreadSplitter();

        List<Instance> instances = new ArrayList<>();
        Model m0 = new DefaultModel();
        m0.getMapping().addReadyVM(m0.newVM(1));
        Node n1 = m0.newNode();
        m0.getMapping().addOnlineNode(n1);
        m0.getMapping().addRunningVM(m0.newVM(2), n1);
        Model m1 = new DefaultModel();
        m1.getMapping().addReadyVM(m1.newVM(3));
        Node n2 = m1.newNode();
        Node n3 = m1.newNode();
        m1.getMapping().addOnlineNode(n2);
        m1.getMapping().addOnlineNode(n3);
        m1.getMapping().addSleepingVM(m1.newVM(4), n2);
        m1.getMapping().addRunningVM(m1.newVM(5), n3);

        instances.add(new Instance(m0, new ArrayList<SatConstraint>(), new MinMTTR()));
        instances.add(new Instance(m1, new ArrayList<SatConstraint>(), new MinMTTR()));

        Set<VM> all = new HashSet<>(m0.getMapping().getAllVMs());
        all.addAll(m1.getMapping().getAllVMs());

        TIntIntHashMap index = Instances.makeVMIndex(instances);

        //Only VMs in m0
        Spread spreadSingle = new Spread(m0.getMapping().getAllVMs());
        Assert.assertTrue(splitter.split(spreadSingle, null, instances, index, new TIntIntHashMap()));
        Assert.assertTrue(instances.get(0).getSatConstraints().contains(spreadSingle));
        Assert.assertFalse(instances.get(1).getSatConstraints().contains(spreadSingle));

        //All the VMs, test the split
        Spread spreadAmong = new Spread(all, false);

        Assert.assertTrue(splitter.split(spreadAmong, null, instances, index, new TIntIntHashMap()));
        Assert.assertTrue(instances.get(0).getSatConstraints().contains(new Spread(m0.getMapping().getAllVMs(), false)));
        Assert.assertTrue(instances.get(1).getSatConstraints().contains(new Spread(m1.getMapping().getAllVMs(), false)));
    }
}
