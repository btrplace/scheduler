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

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.*;
import org.btrplace.model.constraint.*;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.MappingFiller;
import org.btrplace.scheduler.choco.duration.ConstantActionDuration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CRunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CCRunningCapacityTest {

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
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n3, vm3, vm4)
                .sleep(n2, vm5).get();
        List<SatConstraint> l = new ArrayList<>();
        org.btrplace.model.constraint.RunningCapacity x = new org.btrplace.model.constraint.RunningCapacity(map.getAllNodes(), 4);
        x.setContinuous(false);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantActionDuration(10));
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
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4, vm5).get();
        Set<Node> on = new HashSet<>(Arrays.asList(n1, n2));
        List<SatConstraint> l = new ArrayList<>();
        org.btrplace.model.constraint.RunningCapacity x = new org.btrplace.model.constraint.RunningCapacity(on, 4);
        x.setContinuous(false);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantActionDuration(10));
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
        new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4).ready(vm5).get();
        Set<Node> on = new HashSet<>(Arrays.asList(n1, n2));
        List<SatConstraint> l = new ArrayList<>();
        l.add(new Running(vm5));
        l.add(new Fence(vm5, Collections.singleton(n1)));
        org.btrplace.model.constraint.RunningCapacity x = new org.btrplace.model.constraint.RunningCapacity(on, 4);
        x.setContinuous(true);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        for (SatConstraint c : l) {
            System.out.println(c);
        }
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantActionDuration(10));
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
        new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .ready(vm1)
                .run(n1, vm2)
                .run(n2, vm3, vm4)
                .run(n3, vm5).get();
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
        org.btrplace.model.constraint.RunningCapacity x = new org.btrplace.model.constraint.RunningCapacity(on, 3);
        x.setContinuous(true);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.setMaxEnd(5);
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantActionDuration(10));
        ReconfigurationPlan plan = cra.solve(mo, l);
        System.out.println(plan);
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
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2, vm3)
                .run(n2, vm4).ready(vm5).get();

        org.btrplace.model.constraint.RunningCapacity c = new org.btrplace.model.constraint.RunningCapacity(map.getAllNodes(), 4);
        CRunningCapacity cc = new CRunningCapacity(c);

        Assert.assertTrue(cc.getMisPlacedVMs(mo).isEmpty());
        map.addRunningVM(vm5, n3);
        Assert.assertEquals(cc.getMisPlacedVMs(mo), map.getAllVMs());
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

        Mapping map = new MappingFiller(model.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3)
                .run(n3, vm4).get();
        Collection<SatConstraint> ctrs = new HashSet<>();
        ctrs.add(new org.btrplace.model.constraint.RunningCapacity(map.getAllNodes(), 2));

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
        Mapping map = new MappingFiller(mo.getMapping()).on(n1).run(n1, vm1, vm2).ready(vm3).get();

        List<SatConstraint> l = new ArrayList<>();
        l.add(new Running(vm1));
        l.add(new Ready(vm2));
        l.add(new Running(vm3));
        RunningCapacity x = new RunningCapacity(Collections.singleton(n1), 2);
        x.setContinuous(false);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantActionDuration(10));
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

        Mapping map = new MappingFiller(mo.getMapping()).on(n1).run(n1, vm1, vm2).ready(vm3).get();
        List<SatConstraint> l = new ArrayList<>();
        l.add(new Running(vm1));
        l.add(new Ready(vm2));
        l.add(new Running(vm3));
        RunningCapacity sc = new RunningCapacity(Collections.singleton(n1), 2);
        sc.setContinuous(true);
        l.add(sc);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.setTimeLimit(3);
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantActionDuration(10));
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
    public void testSingleGetMisplaced() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Mapping m = new MappingFiller(mo.getMapping()).on(n1).run(n1, vm1).ready(vm2, vm4).get();

        RunningCapacity c = new RunningCapacity(Collections.singleton(n1), 1);
        CRunningCapacity cc = new CRunningCapacity(c);

        Assert.assertTrue(cc.getMisPlacedVMs(mo).isEmpty());
        m.addRunningVM(vm4, n1);
        Assert.assertEquals(m.getRunningVMs(n1), cc.getMisPlacedVMs(mo));
    }
}
