/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint;

import org.btrplace.model.DefaultElementBuilder;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.SynchronizedElementBuilder;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Offline;
import org.btrplace.model.constraint.Running;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.runner.disjoint.model.SubModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link StaticPartitioning}.
 *
 * @author Fabien Hermenier
 */
public class StaticPartitioningTest {

    @Test
    public void testInstantiation() {
        StaticPartitioning st = new StaticPartitioning() {
            @Override
            public List<Instance> split(Parameters ps, Instance i) throws SchedulerException {
                throw new UnsupportedOperationException();
            }
        };
        Assert.assertEquals(st.getWorkersCount(), Runtime.getRuntime().availableProcessors());
        st.setWorkersCount(10);
        Assert.assertEquals(st.getWorkersCount(), 10);
    }

    @Test
    public void testParallelSolve() throws SchedulerException {

        SynchronizedElementBuilder eb = new SynchronizedElementBuilder(new DefaultElementBuilder());
        Model origin = new DefaultModel(eb);

        Node n1 = origin.newNode();
        Node n2 = origin.newNode();
        VM vm1 = origin.newVM();
        VM vm2 = origin.newVM();

        /*
         * 2 nodes among 2 instances, 2 VMs to boot on the nodes
         */
        origin.getMapping().addOnlineNode(n1);
        origin.getMapping().addOfflineNode(n2);
        origin.getMapping().addReadyVM(vm1);
        origin.getMapping().addReadyVM(vm2);

        Model s1 = new SubModel(origin, eb, Collections.singletonList(n1), Collections.singleton(vm1));
        Model s2 = new SubModel(origin, eb, Collections.singletonList(n2), Collections.singleton(vm2));

        Instance i0 = new Instance(origin, new MinMTTR());
        final Instance i1 = new Instance(s1, Running.newRunning(Collections.singletonList(vm1)), new MinMTTR());
        final Instance i2 = new Instance(s2, new MinMTTR());
        i2.getSatConstraints().add(new Running(vm2));


        StaticPartitioning st = new StaticPartitioning() {
            @Override
            public List<Instance> split(Parameters ps, Instance i) throws SchedulerException {
                return Arrays.asList(i1, i2);
            }
        };
        Parameters p = new DefaultChocoScheduler();

        ReconfigurationPlan plan = st.solve(p, i0);
        Assert.assertNotNull(plan);
        Model dst = plan.getResult();
        Assert.assertEquals(dst.getMapping().getOnlineNodes().size(), 2);
        Assert.assertEquals(dst.getMapping().getRunningVMs().size(), 2);

        //Now, there is no solution for i2. the resulting plan should be null
        i2.getSatConstraints().addAll(Offline.newOffline(n2));
        plan = st.solve(p, i0);
        Assert.assertNull(plan);
        Assert.assertEquals(st.getStatistics().getSolutions().size(), 0);
    }

    @Test(expectedExceptions = {SchedulerException.class})
    public void testSolvingIncorrectPartitioning() throws SchedulerException {

        SynchronizedElementBuilder eb = new SynchronizedElementBuilder(new DefaultElementBuilder());
        Model origin = new DefaultModel(eb);

        Node n1 = origin.newNode();
        Node n2 = origin.newNode();
        VM vm1 = origin.newVM();
        VM vm2 = origin.newVM();

        /*
         * 2 nodes among 2 instances, 2 VMs to boot on the nodes
         */
        origin.getMapping().addOnlineNode(n1);
        origin.getMapping().addOfflineNode(n2);
        origin.getMapping().addReadyVM(vm1);
        origin.getMapping().addReadyVM(vm2);

        Model s1 = new SubModel(origin, eb, Collections.singletonList(n1), Collections.singleton(vm1));
        Model s2 = new SubModel(origin, eb, Collections.singletonList(n2), Collections.singleton(vm2));

        Instance i0 = new Instance(origin, new MinMTTR());
        final Instance i1 = new Instance(s1, Running.newRunning(Collections.singletonList(vm1)), new MinMTTR());
        final Instance i2 = new Instance(s2, new MinMTTR());
        i2.getSatConstraints().add(new Running(vm1)); //Error, vm1 is in s1, not s2


        StaticPartitioning st = new StaticPartitioning() {
            @Override
            public List<Instance> split(Parameters ps, Instance i) throws SchedulerException {
                return Arrays.asList(i1, i2);
            }
        };
        Parameters p = new DefaultChocoScheduler();
        st.solve(p, i0);
    }
}
