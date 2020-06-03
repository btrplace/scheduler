/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Fence;
import org.btrplace.model.constraint.Gather;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Running;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link Gather}.
 *
 * @author Fabien Hermenier
 */
public class CGatherTest {

    @Test
    public void testDiscreteWithoutRunningVM() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = mo.getMapping().ready(vm1).on(n1, n2).run(n2, vm2);
        Gather g = new Gather(map.getAllVMs());
        g.setContinuous(false);

        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, Collections.singleton(g));
        Assert.assertNotNull(plan);
        Assert.assertEquals(plan.getSize(), 0);
        Model res = plan.getResult();
        Assert.assertTrue(res.getMapping().isReady(vm1));
    }

    @Test
    public void testDiscreteWithRunningVMs() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        Mapping map = mo.getMapping().ready(vm1).on(n1, n2).run(n2, vm2);
        Gather g = new Gather(map.getAllVMs());
        g.setContinuous(false);

        ChocoScheduler cra = new DefaultChocoScheduler();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(g);
        cstrs.addAll(Running.newRunning(map.getAllVMs()));
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
        Model res = plan.getResult();
        Assert.assertEquals(res.getMapping().getVMLocation(vm1), res.getMapping().getVMLocation(vm2));
    }

    @Test
    public void testGetMisplaced() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        Mapping map = mo.getMapping().ready(vm1).on(n1, n2).run(n2, vm2);
        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        Gather g = new Gather(map.getAllVMs());
        CGather c = new CGather(g);
        Assert.assertTrue(c.getMisPlacedVMs(i).isEmpty());
        map.addRunningVM(vm1, n2);
        Assert.assertTrue(c.getMisPlacedVMs(i).isEmpty());

        map.addRunningVM(vm1, n1);
        Assert.assertEquals(c.getMisPlacedVMs(i), map.getAllVMs());
    }

    @Test
    public void testContinuousWithPartialRunning() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = mo.getMapping().ready(vm1).on(n1, n2).run(n2, vm2);
        Gather g = new Gather(map.getAllVMs());
        g.setContinuous(true);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Running.newRunning(map.getAllVMs()));
        cstrs.add(g);
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
    }

    /**
     * We try to relocate co-located VMs in continuous mode. Not allowed
     *
     * @throws org.btrplace.scheduler.SchedulerException
     */
    @Test
    public void testContinuousWithRelocationOfVMs() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = mo.getMapping().on(n1, n2).run(n2, vm1, vm2);
        Gather g = new Gather(map.getAllVMs());
        g.setContinuous(true);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Running.newRunning(map.getAllVMs()));
        cstrs.add(g);
        cstrs.add(new Fence(vm1, Collections.singleton(n1)));
        cstrs.add(new Fence(vm2, Collections.singleton(n1)));
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNull(plan);
    }

    @Test
    public void testContinuousWithNoRunningVMs() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = mo.getMapping().on(n1, n2).ready(vm1, vm2);
        Gather g = new Gather(map.getAllVMs());
        g.setContinuous(true);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(g);
        cstrs.addAll(Running.newRunning(map.getAllVMs()));
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
    }
}
