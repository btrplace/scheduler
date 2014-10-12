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

package org.btrplace.scheduler.choco.runner.disjoint;

import org.btrplace.model.*;
import org.btrplace.model.constraint.MaxOnline;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Running;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.DefaultParameters;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link FixedNodeSetsPartitioning}.
 *
 * @author Fabien Hermenier
 */
public class FixedNodeSetsPartitioningTest {

    private List<Collection<Node>> splitIn(Set<Node> s, int nb) {
        List<Collection<Node>> partOfNodes = new ArrayList<>();
        Set<Node> curPartition = new HashSet<>(nb);
        partOfNodes.add(curPartition);

        for (Node node : s) {
            if (curPartition.size() == nb) {
                curPartition = new HashSet<>(nb);
                partOfNodes.add(curPartition);
            }
            curPartition.add(node);
        }
        return partOfNodes;
    }

    private Instance makeInstance() {
        Model mo = new DefaultModel();
        for (int i = 0; i < 20; i++) {
            Node n = mo.newNode();
            mo.getMapping().addOnlineNode(n);
            VM v = mo.newVM();
            mo.getMapping().addRunningVM(v, n); //1 VM per node is already running
        }
        //30 VMs to launch
        for (int i = 0; i < 30; i++) {
            VM v = mo.newVM();
            mo.getMapping().addReadyVM(v);
        }

        return new Instance(mo, (List) Running.newRunning(mo.getMapping().getAllVMs()), new MinMTTR());
    }

    @Test
    public void testInstantiation() throws SchedulerException {
        Instance origin = makeInstance();

        List<Collection<Node>> parts = splitIn(origin.getModel().getMapping().getAllNodes(), 3);
        FixedNodeSetsPartitioning f = new FixedNodeSetsPartitioning(parts);
        Assert.assertEquals(f.getPartitions(), parts);
        parts = splitIn(origin.getModel().getMapping().getAllNodes(), 5);
        Assert.assertTrue(f.setPartitions(parts));
        Assert.assertEquals(f.getPartitions(), parts);

        //Make the set not disjoint
        parts.get(1).addAll(parts.get(0));
        Assert.assertFalse(f.setPartitions(parts));
    }

    @Test
    public void testSplit() throws SchedulerException {

        Instance origin = makeInstance();

        List<Collection<Node>> parts = splitIn(origin.getModel().getMapping().getAllNodes(), 3);
        FixedNodeSetsPartitioning f = new FixedNodeSetsPartitioning(parts);
        f.setWorkersCount(3);

        List<Instance> subs = f.split(new DefaultParameters(), origin);
        //Check disjoint set of ready VMs
        Set<VM> allReady = new HashSet<>();
        for (Instance i : subs) {
            allReady.addAll(i.getModel().getMapping().getReadyVMs());
        }
        Assert.assertEquals(allReady.size(), 30);

        //Quick solve
        DefaultChocoScheduler cra = new DefaultChocoScheduler();
        cra.setInstanceSolver(f);
        ReconfigurationPlan plan = cra.solve(origin);
        Assert.assertEquals(plan.getSize(), 30); //all the VMs to launch have been booted
        System.out.println(cra.getStatistics());
        System.out.flush();
    }

    @Test(expectedExceptions = {SchedulerException.class})
    public void testSplitWithUnsplittableConstraint() throws SchedulerException {
        Instance orig = makeInstance();
        orig.getSatConstraints().add(new MaxOnline(orig.getModel().getMapping().getAllNodes(), 5));
        List<Collection<Node>> parts = splitIn(orig.getModel().getMapping().getAllNodes(), 3);
        FixedNodeSetsPartitioning f = new FixedNodeSetsPartitioning(parts);
        f.split(new DefaultParameters(), orig);
    }
}
