/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint.splitter;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Spread;
import org.btrplace.scheduler.runner.disjoint.Instances;
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

        instances.add(new Instance(m0, new ArrayList<>(), new MinMTTR()));
        instances.add(new Instance(m1, new ArrayList<>(), new MinMTTR()));

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
