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
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Overbook;
import org.btrplace.model.constraint.Preserve;
import org.btrplace.model.constraint.ResourceCapacity;
import org.btrplace.model.constraint.Running;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link CRunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CResourceCapacityTest {

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

        ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 3);
        rc.setConsumption(vm4, 1);
        rc.setConsumption(vm5, 5);

        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<>();
        ResourceCapacity x = new ResourceCapacity(map.getAllNodes(), "cpu", 10);
        x.setContinuous(false);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
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

        ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 3);
        rc.setConsumption(vm4, 1);
        rc.setConsumption(vm5, 1);

        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<>();
        ResourceCapacity x = new ResourceCapacity(on, "cpu", 9);
        x.setContinuous(false);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        Assert.assertTrue(plan.getSize() > 0);
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
                .run(n2, vm3, vm4)
                .ready(vm5);
        Set<Node> on = new HashSet<>(Arrays.asList(n1, n2));

      ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 3);
        rc.setConsumption(vm4, 1);
        rc.setConsumption(vm5, 5);
        mo.attach(rc);

        List<SatConstraint> l = new ArrayList<>();
        l.add(new Running(vm5));
        l.add(new Fence(vm5, Collections.singleton(n1)));
        ResourceCapacity x = new ResourceCapacity(on, "cpu", 10);
        x.setContinuous(true);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        //System.out.println(plan);
        Assert.assertTrue(plan.getSize() > 0);
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
        Mapping m = mo.getMapping().on(n1, n2, n3)
                .run(n1, vm1, vm2, vm3).run(n2, vm4).ready(vm5);

        ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 3);
        rc.setConsumption(vm4, 1);
        rc.setConsumption(vm5, 5);
        mo.attach(rc);
        ResourceCapacity c = new ResourceCapacity(m.getAllNodes(), "cpu", 10);
        CResourceCapacity cc = new CResourceCapacity(c);

        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        Assert.assertTrue(cc.getMisPlacedVMs(i).isEmpty());
        m.addRunningVM(vm5, n3);
        Assert.assertEquals(cc.getMisPlacedVMs(i), m.getAllVMs());
    }

    /*@Test
    public void testSingleGetMisplaced() {
        Model mo = new DefaultModel();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n2 = mo.newNode();
        Mapping map = mo.getMapping().on(n2).run(n2, vm2, vm3);

        org.btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 1);

        mo.attach(rc);

        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        ResourceCapacity s = new ResourceCapacity(n2, "cpu", 4);
        CResourceCapacity cs = new CResourceCapacity(s);
        Assert.assertTrue(cs.getMisPlacedVMs(i).isEmpty());
        rc.setConsumption(vm3, 2);
        Assert.assertEquals(cs.getMisPlacedVMs(i), map.getRunningVMs(n2));
    }*/

    @Test
    public void testDiscreteSolvable() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        mo.getMapping().on(n1, n2).run(n1, vm1, vm2);

        ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 1);
        rc.setConsumption(vm3, 1);

        mo.attach(rc);

        ResourceCapacity s = new ResourceCapacity(n1, "cpu", 4);

        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan p = cra.solve(mo, Arrays.asList(s, new Preserve(vm2, "cpu", 3)));
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 1);
        //System.out.println(p);
    }

    @Test
    public void testDiscreteUnsolvable() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();

        mo.getMapping().on(n1).run(n1, vm1, vm2);

      ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 2);

        mo.attach(rc);

        ResourceCapacity s = new ResourceCapacity(n1, "cpu", 3);

        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan p = cra.solve(mo, Collections.singleton(s));
        Assert.assertNull(p);
    }

    @Test
    public void testSingleContinuousSolvable() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = mo.getMapping().on(n1, n2).run(n1, vm1, vm2).ready(vm4);
        ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 1);
        rc.setConsumption(vm3, 1);
        rc.setConsumption(vm4, 3);

        mo.attach(rc);

        List<SatConstraint> cstrs = new ArrayList<>();

        ResourceCapacity s = new ResourceCapacity(n1, "cpu", 4);
        s.setContinuous(true);

        cstrs.add(s);
        cstrs.add(new Fence(vm4, Collections.singleton(n1)));
        cstrs.add(new Running(vm4));
        cstrs.addAll(Overbook.newOverbooks(map.getAllNodes(), "cpu", 1));
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 2);
    }

    /**
     * Test how CResourceCapacity notifies the view about the future capacity.
     */
    @Test
    public void testCapacityEstimation() {
        final Model mo = new DefaultModel();
        final Node n0 = mo.newNode();
        final Node n1 = mo.newNode();
        final VM vm = mo.newVM();

        final ShareableResource cpu = new ShareableResource("cpu");
        mo.attach(cpu);
        cpu.setCapacity(n0, 5).setCapacity(n1, 7);
        cpu.setConsumption(vm, 3);
        mo.getMapping().on(n0, n1).ready(vm);
        final List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Running(vm));

        final Instance i = new Instance(mo, cstrs, new MinMTTR());
        // Because of the worst-fit approach, the VM will go on n0 which has the
        // highest capacity.
        final ChocoScheduler sched = new DefaultChocoScheduler();
        ReconfigurationPlan res = sched.solve(i);
        Assert.assertEquals(n1, res.getResult().getMapping().getVMLocation(vm));

        // With a ResourceCapacity, we restrict n1 so that now n0 is the node
        // that will have the highest capacity. So the wort-fit approach inside
        // MinMTTR() should pick n0.
        i.getSatConstraints().add(new ResourceCapacity(n1, "cpu", 4));
        res = sched.solve(i);
        Assert.assertEquals(n0, res.getResult().getMapping().getVMLocation(vm));
    }
}
