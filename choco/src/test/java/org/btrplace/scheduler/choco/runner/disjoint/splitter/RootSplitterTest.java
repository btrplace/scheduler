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
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Root;
import org.btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link org.btrplace.scheduler.choco.runner.disjoint.splitter.RootSplitter}.
 *
 * @author Fabien Hermenier
 */
public class RootSplitterTest {

    @Test
    public void simpleTest() {
        RootSplitter splitter = new RootSplitter();

        List<Instance> instances = new ArrayList<>();
        Model m0 = new DefaultModel();
        VM v = m0.newVM(1);
        m0.getMapping().addReadyVM(v);
        m0.getMapping().addRunningVM(m0.newVM(2), m0.newNode(1));
        Model m1 = new DefaultModel();
        m1.getMapping().addReadyVM(m1.newVM(3));
        m1.getMapping().addSleepingVM(m1.newVM(4), m1.newNode(2));
        m1.getMapping().addRunningVM(m1.newVM(5), m1.newNode(3));


        instances.add(new Instance(m0, new ArrayList<SatConstraint>(), new MinMTTR()));
        instances.add(new Instance(m1, new ArrayList<SatConstraint>(), new MinMTTR()));

        Set<VM> all = new HashSet<>(m0.getMapping().getAllVMs());
        all.addAll(m1.getMapping().getAllVMs());

        TIntIntHashMap index = Instances.makeVMIndex(instances);

        //Only VMs in m0
        Root single = new Root(v);
        Assert.assertTrue(splitter.split(single, null, instances, index, new TIntIntHashMap()));
        Assert.assertTrue(instances.get(0).getSatConstraints().contains(single));
        Assert.assertFalse(instances.get(1).getSatConstraints().contains(single));
    }
}
