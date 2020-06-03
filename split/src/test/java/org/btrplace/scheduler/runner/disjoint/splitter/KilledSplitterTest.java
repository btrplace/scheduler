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
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Killed;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.scheduler.runner.disjoint.Instances;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link KilledSplitter}.
 *
 * @author Fabien Hermenier
 */
public class KilledSplitterTest {

    @Test
    public void simpleTest() {
        KilledSplitter splitter = new KilledSplitter();

        List<Instance> instances = new ArrayList<>();
        Model m0 = new DefaultModel();
        VM v = m0.newVM(1);
        m0.getMapping().addReadyVM(v);
        m0.getMapping().addRunningVM(m0.newVM(2), m0.newNode(1));
        Model m1 = new DefaultModel();
        m1.getMapping().addReadyVM(m1.newVM(3));
        m1.getMapping().addSleepingVM(m1.newVM(4), m1.newNode(2));
        m1.getMapping().addRunningVM(m1.newVM(5), m1.newNode(3));


        instances.add(new Instance(m0, new ArrayList<>(), new MinMTTR()));
        instances.add(new Instance(m1, new ArrayList<>(), new MinMTTR()));

        TIntIntHashMap index = Instances.makeVMIndex(instances);

        Set<VM> all = new HashSet<>(m0.getMapping().getAllVMs());
        all.addAll(m1.getMapping().getAllVMs());


        //Only VMs in m0
        Killed single = new Killed(v);
        Assert.assertTrue(splitter.split(single, null, instances, index, new TIntIntHashMap()));
        Assert.assertTrue(instances.get(0).getSatConstraints().contains(single));
        Assert.assertFalse(instances.get(1).getSatConstraints().contains(single));
    }
}
