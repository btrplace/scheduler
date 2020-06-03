/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.json.JSON;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Among;
import org.btrplace.model.constraint.Fence;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Ready;
import org.btrplace.model.constraint.Root;
import org.btrplace.model.constraint.Running;
import org.btrplace.model.constraint.RunningCapacity;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.Seq;
import org.btrplace.model.constraint.Sleeping;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.duration.ConstantActionDuration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Unit tests for {@link CRunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CRunningCapacityTest {

    @Test
    public void testWithSatisfiedConstraint() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Mapping map = mo.getMapping().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n3, vm3, vm4)
                .sleep(n2, vm5);
        List<SatConstraint> l = new ArrayList<>();
        RunningCapacity x = new RunningCapacity(map.getAllNodes(), 4);
        x.setContinuous(false);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantActionDuration<>(10));
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertEquals(plan.getSize(), 0);
    }

    @Test
    public void testDiscreteSatisfaction() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        mo.getMapping().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4, vm5);
        Set<Node> on = new HashSet<>(Arrays.asList(n1, n2));
        List<SatConstraint> l = new ArrayList<>();
        RunningCapacity x = new RunningCapacity(on, 4);
        x.setContinuous(false);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantActionDuration<>(10));
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        Assert.assertEquals(plan.getSize(), 1);
    }


    @Test
    public void testFeasibleContinuousResolution() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        mo.getMapping().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4).ready(vm5);
        Set<Node> on = new HashSet<>(Arrays.asList(n1, n2));
        List<SatConstraint> l = new ArrayList<>();
        l.add(new Running(vm5));
        l.add(new Fence(vm5, Collections.singleton(n1)));
        RunningCapacity x = new RunningCapacity(on, 4);
        x.setContinuous(true);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();

        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantActionDuration<>(10));
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        Assert.assertEquals(plan.getSize(), 2);
    }

    @Test
    public void testUnFeasibleContinuousResolution() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        mo.getMapping().on(n1, n2, n3)
                .ready(vm1)
                .run(n1, vm2)
                .run(n2, vm3, vm4)
                .run(n3, vm5);
        List<SatConstraint> l = new ArrayList<>();

        List<VM> seq = new ArrayList<>();
        seq.add(vm1);
        seq.add(vm2);
        l.add(new Seq(seq));
        l.add(new Fence(vm1, Collections.singleton(n1)));
        l.add(new Sleeping(vm2));
        l.add(new Running(vm1));
        l.add(new Root(vm3));
        l.add(new Root(vm4));

        Set<Node> on = new HashSet<>(Arrays.asList(n1, n2));
        RunningCapacity x = new RunningCapacity(on, 3);
        x.setContinuous(true);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.setMaxEnd(5);
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantActionDuration<>(10));
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNull(plan);
    }

    @Test
    public void testGetMisplaced() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Mapping map = mo.getMapping().on(n1, n2, n3)
                .run(n1, vm1, vm2, vm3)
                .run(n2, vm4).ready(vm5);

        RunningCapacity c = new RunningCapacity(map.getAllNodes(), 4);
        CRunningCapacity cc = new CRunningCapacity(c);

        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        Assert.assertTrue(cc.getMisPlacedVMs(i).isEmpty());
        map.addRunningVM(vm5, n3);
        Assert.assertEquals(cc.getMisPlacedVMs(i), map.getAllVMs());
    }

    @Test
    public void testUnfeasible() throws SchedulerException {

        Model model = new DefaultModel();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();

        Mapping map = model.getMapping().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3)
                .run(n3, vm4);
        Collection<SatConstraint> ctrs = new HashSet<>();
        ctrs.add(new RunningCapacity(map.getAllNodes(), 2));

        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(model, ctrs);
        Assert.assertNull(plan);
    }

    @Test
    public void testSingleDiscreteResolution() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        mo.getMapping().on(n1).run(n1, vm1, vm2).ready(vm3);

        List<SatConstraint> l = new ArrayList<>();
        l.add(new Running(vm1));
        l.add(new Ready(vm2));
        l.add(new Running(vm3));
        RunningCapacity x = new RunningCapacity(Collections.singleton(n1), 2);
        x.setContinuous(false);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantActionDuration<>(10));
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertEquals(2, plan.getSize());
    }

    @Test
    public void testSingleContinuousResolution() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();

        mo.getMapping().on(n1).run(n1, vm1, vm2).ready(vm3);
        List<SatConstraint> l = new ArrayList<>();
        l.add(new Running(vm1));
        l.add(new Ready(vm2));
        l.add(new Running(vm3));
        RunningCapacity sc = new RunningCapacity(Collections.singleton(n1), 2, true);
        sc.setContinuous(true);
        l.add(sc);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.setTimeLimit(3);
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantActionDuration<>(10));
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        Iterator<Action> ite = plan.iterator();
        Assert.assertEquals(2, plan.getSize());
        Action a1 = ite.next();
        Action a2 = ite.next();
        Assert.assertTrue(a1 instanceof ShutdownVM);
        Assert.assertTrue(a2 instanceof BootVM);
        Assert.assertTrue(a1.getEnd() <= a2.getStart());
    }

    @Test
    public void testIssue112() throws Exception {
        String buf = "{\"model\":{\"mapping\":{\"readyVMs\":[],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[2,1]},\"2\":{\"sleepingVMs\":[],\"runningVMs\":[]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{}},\"views\":[]},\"constraints\":[{\"vm\":0,\"continuous\":false,\"id\":\"running\"},{\"vm\":1,\"continuous\":false,\"id\":\"running\"},{\"vm\":2,\"continuous\":false,\"id\":\"running\"},{\"amount\":2,\"nodes\":[0],\"continuous\":false,\"id\":\"runningCapacity\"},{\"amount\":1,\"nodes\":[1],\"continuous\":false,\"id\":\"runningCapacity\"},{\"amount\":0,\"nodes\":[2],\"continuous\":false,\"id\":\"runningCapacity\"},{\"continuous\":false,\"parts\":[[0,1,2]],\"id\":\"among\",\"vms\":[0]},{\"continuous\":false,\"parts\":[[0,1,2]],\"id\":\"among\",\"vms\":[1]},{\"continuous\":false,\"parts\":[[0,1,2]],\"id\":\"among\",\"vms\":[2]}],\"objective\":{\"id\":\"minimizeMTTR\"}}";
        Instance i = JSON.readInstance(new StringReader(buf));
        List<SatConstraint> l = i.getSatConstraints().stream().filter(s -> !(s instanceof Among)).collect(Collectors.toList());
        ChocoScheduler s = new DefaultChocoScheduler();

        i = new Instance(i.getModel(), l, new MinMTTR());
        ReconfigurationPlan p = s.solve(i);
        Assert.assertNotNull(p);
    }

    @Test
    public void testSingleGetMisplaced() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Mapping m = mo.getMapping().on(n1).run(n1, vm1).ready(vm2, vm4);

        RunningCapacity c = new RunningCapacity(Collections.singleton(n1), 1);
        CRunningCapacity cc = new CRunningCapacity(c);

        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        Assert.assertTrue(cc.getMisPlacedVMs(i).isEmpty());
        m.addRunningVM(vm4, n1);
        Assert.assertEquals(m.getRunningVMs(n1), cc.getMisPlacedVMs(i));
    }
}
