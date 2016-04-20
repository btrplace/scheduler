/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

import org.btrplace.model.*;
import org.btrplace.model.constraint.*;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.*;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
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
        Assert.assertEquals(-1, rcm.getVMsAllocation().get(rp.getVM(vm1)).getLB());
        Assert.assertEquals(-1, rcm.getVMsAllocation().get(rp.getVM(vm2)).getLB());
        Assert.assertEquals(0, rcm.getVMsAllocation().get(rp.getVM(vm3)).getUB()); //Will not be running so 0
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
    public void testRealNodeUsage() throws SchedulerException, ContradictionException {
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();

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
        rc = (ShareableResource) res.getView(ShareableResource.VIEW_ID_BASE + "foo");
        Assert.assertEquals(2, rc.getConsumption(vm1));//rcm.getVirtualUsage(0).isInstantiatedTo(2));
        Assert.assertEquals(3, rc.getConsumption(vm2));//rcm.getVirtualUsage(1).isInstantiatedTo(3));
    }

    @Test
    public void testMaintainResourceUsage() throws SchedulerException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();

        Node n1 = mo.newNode();

        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        ShareableResource rc = new ShareableResource("foo");
        rc.setConsumption(vm1, 5);
        rc.setConsumption(vm2, 7);
        rc.setCapacity(n1, 25);

        mo.attach(rc);

        Parameters ps = new DefaultParameters();
        ChocoScheduler s = new DefaultChocoScheduler();
        ReconfigurationPlan p = s.solve(mo, new ArrayList<>());
        Assert.assertNotNull(p);

        //And on the resulting plan.
        Model res = p.getResult();
        ShareableResource resRc = (ShareableResource) res.getView(rc.getIdentifier());
        Assert.assertEquals(resRc.getConsumption(vm1), 5);
        Assert.assertEquals(resRc.getConsumption(vm2), 7);
    }

    /**
     * The default overbooking ratio of 1 will make this problem having no solution.
     */
    @Test
    public void testDefaultOverbookRatio() throws ContradictionException, SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();

        Mapping ma = new MappingFiller(mo.getMapping()).on(n1).run(n1, vm1, vm2).get();

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

        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).run(n1, vm1, vm2).get();

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
        System.out.println(p);
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
            Assert.assertEquals(s.getStatistics().getNbBacktracks(), 0);
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
        Assert.assertEquals(p.getResult().getMapping(), mo.getMapping());
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 2);
    }
}
