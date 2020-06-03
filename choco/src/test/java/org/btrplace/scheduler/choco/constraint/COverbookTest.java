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
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Online;
import org.btrplace.model.constraint.Overbook;
import org.btrplace.model.constraint.Preserve;
import org.btrplace.model.constraint.Ready;
import org.btrplace.model.constraint.Running;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.Sleeping;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.duration.LinearToAResourceActionDuration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Unit tests for {@link COverbook}.
 *
 * @author Fabien Hermenier
 */
public class COverbookTest {

    @Test
    public void testBasic() throws SchedulerException {
        Node[] nodes = new Node[2];
        VM[] vms = new VM[3];
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        ShareableResource rcCPU = new ShareableResource("cpu");
        for (int i = 0; i < vms.length; i++) {
            if (i < nodes.length) {
                nodes[i] = mo.newNode();
                rcCPU.setCapacity(nodes[i], 2);
                m.addOnlineNode(nodes[i]);
            }
            vms[i] = mo.newVM();
            rcCPU.setConsumption(vms[i], 1);

            m.addReadyVM(vms[i]);
        }
        mo.attach(rcCPU);
        Overbook o = new Overbook(nodes[0], "cpu", 2);
        Overbook o2 = new Overbook(nodes[1], "cpu", 2);
        Collection<SatConstraint> c = new HashSet<>();
        c.add(o);
        c.add(o2);
        c.addAll(Running.newRunning(m.getAllVMs()));
        c.add(new Preserve(vms[0], "cpu", 1));
        c.addAll(Online.newOnline(m.getAllNodes()));
        DefaultChocoScheduler cra = new DefaultChocoScheduler();
        cra.getMapper().mapConstraint(Overbook.class, COverbook.class);
        ReconfigurationPlan p = cra.solve(mo, c);
        Assert.assertNotNull(p);
    }

    /**
     * One overbook factor per node.
     *
     * @throws org.btrplace.scheduler.SchedulerException should not occur
     */
    @Test
    public void testMultipleOverbook() throws SchedulerException {
        Node[] nodes = new Node[3];
        VM[] vms = new VM[11];
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        ShareableResource rcCPU = new ShareableResource("cpu");
        for (int i = 0; i < vms.length; i++) {
            if (i < nodes.length) {
                nodes[i] = mo.newNode();
                rcCPU.setCapacity(nodes[i], 2);
                m.addOnlineNode(nodes[i]);
            }
            vms[i] = mo.newVM();
            rcCPU.setConsumption(vms[i], 1);

            m.addReadyVM(vms[i]);
        }
        mo.attach(rcCPU);
        Collection<SatConstraint> c = new HashSet<>();
        c.add(new Overbook(nodes[0], "cpu", 1));
        c.add(new Overbook(nodes[1], "cpu", 2));
        c.add(new Overbook(nodes[2], "cpu", 3));
        c.addAll(Running.newRunning(m.getAllVMs()));
        c.add(new Preserve(vms[0], "cpu", 1));
        DefaultChocoScheduler cra = new DefaultChocoScheduler();
        cra.setTimeLimit(-1);
        ReconfigurationPlan p = cra.solve(mo, c);
        Assert.assertNotNull(p);
    }

    @Test
    public void testNoSolution() throws SchedulerException {
        Node[] nodes = new Node[2];
        VM[] vms = new VM[7];
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        ShareableResource rcMem = new ShareableResource("mem");
        for (int i = 0; i < vms.length; i++) {
            if (i < nodes.length) {
                nodes[i] = mo.newNode();
                rcMem.setCapacity(nodes[i], 3);
                m.addOnlineNode(nodes[i]);
            }
            vms[i] = mo.newVM();
            rcMem.setConsumption(vms[i], 1);
            m.addReadyVM(vms[i]);
        }
        mo.attach(rcMem);
        Collection<SatConstraint> c = new HashSet<>();
        c.add(new Overbook(nodes[0], "mem", 1));
        c.add(new Overbook(nodes[1], "mem", 1));
        c.addAll(Running.newRunning(m.getAllVMs()));
        for (VM v : vms) {
            c.add(new Preserve(v, "mem", 1));
        }

        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.getDurationEvaluators().register(BootVM.class, new LinearToAResourceActionDuration<VM>("mem", 2, 3));
        Assert.assertNull(cra.solve(mo, c));
    }

    @Test
    public void testGetMisplaced() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        mo.getMapping().on(n1, n2, n3)
                .run(n1, vm1)
                .run(n2, vm2, vm3)
                .run(n3, vm4, vm5, vm6);
        ShareableResource rcCPU = new ShareableResource("cpu", 1, 1);
        mo.attach(rcCPU);
        Overbook o1 = new Overbook(n1, "cpu", 1);
        Overbook o2 = new Overbook(n2, "cpu", 2);
        Overbook o3 = new Overbook(n3, "cpu", 3);
        COverbook co1 = new COverbook(o1);
        COverbook co2 = new COverbook(o2);
        COverbook co3 = new COverbook(o3);
        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        Assert.assertTrue(co1.getMisPlacedVMs(i).isEmpty());
        Assert.assertTrue(co2.getMisPlacedVMs(i).isEmpty());
        Assert.assertEquals(o3.getInvolvedVMs(), co3.getMisPlacedVMs(i));
    }


    @Test
    public void testWithScheduling1() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Mapping m = mo.getMapping().on(n1).run(n1, vm1).ready(vm3);

        ShareableResource rcCPU = new ShareableResource("cpu", 2, 2);

        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Running(vm3));
        cstrs.add(new Sleeping(vm1));
        cstrs.addAll(Online.newOnline(m.getAllNodes()));
        cstrs.add(new Overbook(n1, "cpu", 1));
        cstrs.add(new Preserve(vm1, "cpu", 2));
        cstrs.add(new Preserve(vm3, "cpu", 2));
        mo.attach(rcCPU);

        ChocoScheduler cra = new DefaultChocoScheduler();

        ReconfigurationPlan p = cra.solve(mo, cstrs);

        Assert.assertNotNull(p);
    }

    /**
     * Test with a root VM that has increasing need and another one that prevent it
     * to get the resources immediately
     */
    @Test
    public void testWithIncrease() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Mapping map = mo.getMapping().on(n1).run(n1, vm1, vm2);

        ShareableResource rc = new ShareableResource("foo");
        rc.setCapacity(n1, 5);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 2);
        mo.attach(rc);

        ChocoScheduler cra = new DefaultChocoScheduler();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Online.newOnline(map.getAllNodes()));
        Overbook o = new Overbook(n1, "foo", 1);
        o.setContinuous(true);
        cstrs.add(o);
        cstrs.add(new Ready(vm2));
        cstrs.add(new Preserve(vm1, "foo", 5));
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);

        Assert.assertEquals(p.getSize(), 2);
        Action al = p.getActions().stream().filter(s -> s instanceof Allocate).findAny().get();
        Action sh = p.getActions().stream().filter(s -> s instanceof ShutdownVM).findAny().get();
        Assert.assertTrue(sh.getEnd() <= al.getStart());
    }
}
