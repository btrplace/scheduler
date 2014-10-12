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
import org.btrplace.model.constraint.Ban;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link org.btrplace.scheduler.choco.runner.disjoint.splitter.BanSplitter}.
 *
 * @author Fabien Hermenier
 */
public class BanSplitterTest {

    @Test
    public void simpleTest() {
        BanSplitter splitter = new BanSplitter();

        List<Instance> instances = new ArrayList<>();
        Model origin = new DefaultModel();
        Node n1 = origin.newNode();
        Node n2 = origin.newNode();
        VM vm1 = origin.newVM();
        VM vm2 = origin.newVM();
        VM vm3 = origin.newVM();
        VM vm4 = origin.newVM();

        /**
         * READY: vm1
         * n1 vm2
         * n2 (vm3) vm4
         */
        origin.getMapping().addOnlineNode(n1);
        origin.getMapping().addReadyVM(vm1);
        origin.getMapping().addRunningVM(vm2, n1);
        origin.getMapping().addOnlineNode(n2);
        origin.getMapping().addSleepingVM(vm3, n2);
        origin.getMapping().addRunningVM(vm4, n2);


        Model m0 = new DefaultModel();
        m0.newNode(n1.id());
        m0.newVM(vm1.id());
        m0.newVM(vm2.id());
        m0.getMapping().addOnlineNode(n1);
        m0.getMapping().addReadyVM(vm1);
        m0.getMapping().addRunningVM(vm2, n1);

        Model m1 = new DefaultModel();
        m1.newNode(n2.id());
        m1.newVM(vm3.id());
        m1.newVM(vm4.id());
        m1.getMapping().addOnlineNode(n2);
        m1.getMapping().addSleepingVM(vm3, n2);
        m1.getMapping().addRunningVM(vm4, n2);

        instances.add(new Instance(m0, new ArrayList<SatConstraint>(), new MinMTTR()));
        instances.add(new Instance(m1, new ArrayList<SatConstraint>(), new MinMTTR()));

        Set<VM> all = new HashSet<>(m0.getMapping().getAllVMs());
        all.addAll(m1.getMapping().getAllVMs());

        TIntIntHashMap vmIndex = Instances.makeVMIndex(instances);
        TIntIntHashMap nodeIndex = Instances.makeNodeIndex(instances);

        //Only VMs & nodes in m0
        Ban single = new Ban(vm1, m0.getMapping().getAllNodes());
        Assert.assertTrue(splitter.split(single, null, instances, vmIndex, nodeIndex));
        Assert.assertTrue(instances.get(0).getSatConstraints().contains(single));
        Assert.assertFalse(instances.get(1).getSatConstraints().contains(single));

        //All the VMs, nodes in m1.
        Ban among = new Ban(vm3, m1.getMapping().getAllNodes());

        Assert.assertTrue(splitter.split(among, null, instances, vmIndex, nodeIndex));
        Assert.assertTrue(instances.get(0).getSatConstraints().contains(new Ban(vm1, m0.getMapping().getAllNodes())));
        Assert.assertTrue(instances.get(1).getSatConstraints().contains(new Ban(vm3, m1.getMapping().getAllNodes())));
    }
}
