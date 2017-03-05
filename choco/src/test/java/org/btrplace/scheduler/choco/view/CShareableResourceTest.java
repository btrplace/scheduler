/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco.view;

import org.btrplace.json.JSON;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Fence;
import org.btrplace.model.constraint.NoDelay;
import org.btrplace.model.constraint.Online;
import org.btrplace.model.constraint.Overbook;
import org.btrplace.model.constraint.Preserve;
import org.btrplace.model.constraint.Running;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.DefaultParameters;
import org.btrplace.scheduler.choco.DefaultReconfigurationProblemBuilder;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link CShareableResource}.
 *
 * @author Fabien Hermenier
 */
public class CShareableResourceTest {

    /**
     * Test the instantiation and the creation of the variables.
     *
     * @throws org.btrplace.scheduler.SchedulerException should not occur
     */
    @Test
    public void testSimple() throws SchedulerException {
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();

        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        ma.addOnlineNode(n1);
        ma.addOfflineNode(n2);
        ma.addRunningVM(vm1, n1);
        ma.addRunningVM(vm2, n1);
        ma.addReadyVM(vm3);
        ShareableResource rc = new ShareableResource("foo", 0, 0);
        rc.setConsumption(vm2, 3);
        rc.setCapacity(n1, 4);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).build();
        CShareableResource rcm = new CShareableResource(rc);
        rcm.inject(new DefaultParameters(), rp);
        Assert.assertEquals(rc.getIdentifier(), rcm.getIdentifier());
        //Assert.assertEquals(-1, rcm.getVMsAllocation(rp.getVM(vm1)).getLB());
        Assert.assertEquals(-1, rcm.getVMAllocation(rp.getVM(vm1)));
        //Assert.assertEquals(-1, rcm.getVMsAllocation(rp.getVM(vm2)).getLB());
        Assert.assertEquals(-1, rcm.getVMAllocation(rp.getVM(vm2)));
        //Assert.assertEquals(0, rcm.getVMsAllocation(rp.getVM(vm3)).getUB()); //Will not be running so 0
        Assert.assertEquals(0, rcm.getVMAllocation(rp.getVM(vm3))); //Will not be running so 0
        IntVar pn1 = rcm.getPhysicalUsage().get(rp.getNode(n1));
        IntVar pn2 = rcm.getPhysicalUsage().get(rp.getNode(n2));
        Assert.assertTrue(pn1.getLB() == 0 && pn1.getUB() == 4);
        Assert.assertTrue(pn2.getLB() == 0 && pn2.getUB() == 0);

        pn1 = rcm.getPhysicalUsage(rp.getNode(n1));
        Assert.assertTrue(pn1.getLB() == 0 && pn1.getUB() == 4);

        IntVar vn1 = rcm.getVirtualUsage().get(rp.getNode(n1));
        IntVar vn2 = rcm.getVirtualUsage().get(rp.getNode(n2));
        Assert.assertEquals(vn1.getLB(), 0);
        Assert.assertEquals(vn2.getLB(), 0);

        Assert.assertEquals(rc, rcm.getSourceResource());

    }

    /**
     * Place some VMs and check realNodeUsage is updated accordingly
     */
    @Test
    public void testRealNodeUsage() throws SchedulerException {
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();

        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        ma.addOnlineNode(n1);
        ma.addOnlineNode(n2);
        ma.addRunningVM(vm1, n1);
        ma.addRunningVM(vm2, n1);

        ShareableResource rc = new ShareableResource("foo", 0, 0);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setCapacity(n1, 5);
        rc.setCapacity(n2, 3);
        mo.attach(rc);

        ChocoScheduler s = new DefaultChocoScheduler();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Fence(vm1, n1));
        cstrs.add(new Fence(vm2, n2));
        ReconfigurationPlan p = s.solve(mo, cstrs);
        Assert.assertNotNull(p);
        Model res = p.getResult();
        rc = (ShareableResource.get(res, "foo"));
        Assert.assertEquals(2, rc.getConsumption(vm1));//rcm.getVirtualUsage(0).isInstantiatedTo(2));
        Assert.assertEquals(3, rc.getConsumption(vm2));//rcm.getVirtualUsage(1).isInstantiatedTo(3));
    }

    @Test
    public void testMaintainResourceUsage() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();

        Node n1 = mo.newNode();

        mo.getMapping().on(n1).run(n1, vm1, vm2);
        ShareableResource rc = new ShareableResource("foo");
        rc.setConsumption(vm1, 5);
        rc.setConsumption(vm2, 7);
        rc.setCapacity(n1, 25);

        mo.attach(rc);

        ChocoScheduler s = new DefaultChocoScheduler();
        ReconfigurationPlan p = s.solve(mo, new ArrayList<>());
        Assert.assertNotNull(p);

        //And on the resulting plan.
        Model res = p.getResult();
        ShareableResource resRc = ShareableResource.get(res, rc.getResourceIdentifier());
        Assert.assertEquals(resRc.getConsumption(vm1), 5);
        Assert.assertEquals(resRc.getConsumption(vm2), 7);
    }

    /**
     * The default overbooking ratio of 1 will make this problem having no solution.
     */
    @Test
    public void testDefaultOverbookRatio() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();

        mo.getMapping().on(n1).run(n1, vm1, vm2);

        ShareableResource rc = new ShareableResource("foo", 0, 0);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setCapacity(n1, 5);

        mo.attach(rc);

        ChocoScheduler s = new DefaultChocoScheduler();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Fence(vm1, n1));
        cstrs.add(new Preserve(vm2, "foo", 4));
        ReconfigurationPlan p = s.solve(mo, cstrs);//rp.solve(0, false);
        Assert.assertNull(p);
    }

    @Test
    public void testWithFloat() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        Mapping map = mo.getMapping().on(n1, n2).run(n1, vm1, vm2);

        org.btrplace.model.view.ShareableResource rc = new ShareableResource("foo");
        rc.setCapacity(n1, 32);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 2);
        mo.attach(rc);

        ChocoScheduler cra = new DefaultChocoScheduler();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Online.newOnline(map.getAllNodes()));
        Overbook o = new Overbook(n1, "foo", 1.5, false);
        cstrs.add(o);

        Overbook o2 = new Overbook(n2, "foo", 1.5, false);
        cstrs.add(o2);

        cstrs.add(new Preserve(vm1, "foo", 5));
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
    }

    @Test
    public void testInitiallyUnsatisfied() throws SchedulerException {
        Model mo = new DefaultModel();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        ShareableResource rc = new ShareableResource("cpu",1, 1);
        VM v1 = mo.newVM();
        VM v2 = mo.newVM();
        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addOnlineNode(n2);
        mo.getMapping().addRunningVM(v1, n1);
        mo.getMapping().addRunningVM(v2, n1);
        mo.attach(rc);
        ChocoScheduler s = new DefaultChocoScheduler();
        try {
            Assert.assertNull(s.solve(mo, new ArrayList<>()));
            Assert.fail("Should have thrown an exception");
        } catch (SchedulerException e) {
            Assert.assertEquals(s.getStatistics().getMetrics().backtracks(), 0);
        }
    }

    @Test
    public void testMisplaced() throws SchedulerException {
        Model mo = new DefaultModel();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        ShareableResource rc = new ShareableResource("cpu", 10, 1);
        VM v1 = mo.newVM();
        VM v2 = mo.newVM();
        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addOnlineNode(n2);
        mo.getMapping().addRunningVM(v1, n1);
        mo.getMapping().addRunningVM(v2, n1);
        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<>();
        l.addAll(Preserve.newPreserve(mo.getMapping().getAllVMs(), "cpu", 5));
        ChocoScheduler s = new DefaultChocoScheduler();
        s.doRepair(true);
        ReconfigurationPlan p = s.solve(mo, l);
        Assert.assertEquals(s.getStatistics().getNbManagedVMs(), 0);
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getResult().getMapping(), mo.getMapping());
        Assert.assertEquals(p.getSize(), 2);
    }

    //Issue124
    @Test
    public void testEmpty() throws SchedulerException {
        String buf = "{\"model\":{\"mapping\":{\"readyVMs\":[],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[1,0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{}},\"views\":[{\"defConsumption\":0,\"nodes\":{},\"rcId\":\"CPU\",\"id\":\"shareableResource\",\"defCapacity\":0,\"vms\":{}}]},\"constraints\":[{\"continuous\":false,\"id\":\"spread\",\"vms\":[0,1]}],\"objective\":{\"id\":\"minimizeMTTR\"}}";
        Instance i = JSON.readInstance(new StringReader(buf));
        ChocoScheduler s = new DefaultChocoScheduler();
        ReconfigurationPlan p = s.solve(i);
        Assert.assertNotNull(p);
    }

    /**
     * Reproduce issue#145.
     */
    /*@Test*/
    public void testInsertAction() {
        Model mo = new DefaultModel();
        ShareableResource cpu = new ShareableResource("cpu");
        ShareableResource mem = new ShareableResource("mem");
        mo.attach(cpu);
        mo.attach(mem);
        Node node = mo.newNode();
        Node node2 = mo.newNode();
        mo.getMapping().on(node, node2);
        cpu.setCapacity(node, 100000);
        mem.setCapacity(node, 100000);
        cpu.setCapacity(node2, 100000);
        mem.setCapacity(node2, 100000);
        for (int i = 0; i < 10000; i++) {
            VM vm = mo.newVM();
            mo.getMapping().run(node, vm);
            cpu.setConsumption(vm, 1);
            mem.setConsumption(vm, 1);

        }
        ChocoScheduler sched = new DefaultChocoScheduler();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Running.newRunning(mo.getMapping().getAllVMs()));
        cstrs.addAll(NoDelay.newNoDelay(mo.getMapping().getAllVMs()));
        cstrs.addAll(Fence.newFence(mo.getMapping().getAllVMs(), Arrays.asList(node2)));
        cstrs.addAll(Preserve.newPreserve(mo.getMapping().getAllVMs(), "cpu", 2));
        cstrs.addAll(Preserve.newPreserve(mo.getMapping().getAllVMs(), "mem", 2));
        ReconfigurationPlan plan = sched.solve(mo, cstrs);
        System.out.println(plan);
        System.out.println(sched.getStatistics());
    }
}
