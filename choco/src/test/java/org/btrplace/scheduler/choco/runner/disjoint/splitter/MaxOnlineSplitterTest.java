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
import org.btrplace.model.Node;
import org.btrplace.model.constraint.MaxOnline;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.DefaultParameters;
import org.btrplace.scheduler.choco.runner.disjoint.FixedNodeSetsPartitioning;
import org.btrplace.scheduler.choco.runner.disjoint.FixedSizePartitioning;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Unit tests for {@link MaxOnlineSplitter}.
 *
 * @author Fabien Hermenier
 */
public class MaxOnlineSplitterTest {

    @Test
    public void testSplit() throws SchedulerException {
        MaxOnlineSplitter splitter = new MaxOnlineSplitter();

        Model mo = new DefaultModel();
        Node[] ns = new Node[10];
        for (int i = 0; i < ns.length; i++) {
            Node n = mo.newNode();
            mo.getMapping().addOnlineNode(n);
            ns[i] = n;
        }

        FixedNodeSetsPartitioning cut = new FixedSizePartitioning(5);
        Instance origin = new Instance(mo, Collections.<SatConstraint>emptyList(), new MinMTTR());
        List<Instance> instances = cut.split(new DefaultParameters(), origin);
        TIntIntHashMap vmIndex = Instances.makeVMIndex(instances);
        TIntIntHashMap nodeIndex = Instances.makeNodeIndex(instances);

        MaxOnline m1 = new MaxOnline(new HashSet<>(Arrays.asList(ns[0], ns[1], ns[2], ns[3], ns[4])), 3);
        //This one is valid as m1 stay in the first partition
        Assert.assertTrue(splitter.split(m1, origin, instances, vmIndex, nodeIndex));
        boolean found = false;
        for (Instance i : instances) {
            if (i.getSatConstraints().contains(m1)) {
                if (found) {
                    Assert.fail(m1 + " is already in a partition");
                }
                found = true;
            }
        }

        //Invalid, the constraint is over 2 partitions
        MaxOnline m2 = new MaxOnline(new HashSet<>(Arrays.asList(ns[0], ns[1], ns[5])), 3);
        Assert.assertFalse(splitter.split(m2, origin, instances, vmIndex, nodeIndex));
    }
}
