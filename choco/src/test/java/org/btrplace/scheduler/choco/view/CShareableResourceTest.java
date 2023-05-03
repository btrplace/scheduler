/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.view;

import org.btrplace.json.JSON;
import org.btrplace.model.*;
import org.btrplace.model.constraint.*;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.*;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;
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
        Assert.assertEquals(rcm.getFutureVMAllocation(rp.getVM(vm1)), 0);
        Assert.assertEquals(rcm.getFutureVMAllocation(rp.getVM(vm2)), 3);
        Assert.assertEquals(rcm.getFutureVMAllocation(rp.getVM(vm3)), 0); //Will not be running so 0

        Assert.assertEquals(rc.getCapacity(n1),
                rcm.getFutureNodeCapacity(rp.getNode(n1)));
        Assert.assertEquals(rc.getCapacity(n2),
                rcm.getFutureNodeCapacity(rp.getNode(n2)));

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
        Assert.assertEquals(rc.getConsumption(vm1), 2);
        Assert.assertEquals(rc.getConsumption(vm2), 3);
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

        ShareableResource rc = new ShareableResource("foo");
        rc.setCapacity(n1, 32);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 2);
        mo.attach(rc);

        ChocoScheduler cra = new DefaultChocoScheduler();
        List<SatConstraint> cstrs = new ArrayList<>(Online.newOnline(map.getAllNodes()));
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
        } catch (@SuppressWarnings("unused") SchedulerException e) {
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
        List<SatConstraint> l = new ArrayList<>(Preserve.newPreserve(mo.getMapping().getAllVMs(), "cpu", 5));
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

    @Test
    public void testIssue170() {
        String buf = "{\"model\": {\"mapping\": {\"readyVMs\": [],\"onlineNodes\": {\"0\": {\"sleepingVMs\": [],\"runningVMs\": []},\"1\": {\"sleepingVMs\": [],\"runningVMs\": []},\"2\": {\"sleepingVMs\": [],\"runningVMs\": [15, 14, 13, 12,11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0]},\"3\": {\"sleepingVMs\": [],\"runningVMs\": []}},\"offlineNodes\": []},\"attributes\": {\"nodes\": {},\"vms\": {\"11\": {\"migrate\": 1},\"12\": {\"migrate\": 1},\"13\": {\"migrate\": 1},\"14\": {\"migrate\": 1},\"15\": {\"migrate\": 1},\"0\": {\"migrate\": 1},\"1\": {\"migrate\": 1},\"2\": {\"migrate\": 1},\"3\": {\"migrate\": 1},\"4\": {\"migrate\": 1},\"5\": {\"migrate\": 1},\"6\": {\"migrate\": 2},\"7\": {\"migrate\": 1},\"8\": {\"migrate\": 1},\"9\": {\"migrate\": 1},\"10\": {\"migrate\": 1}}},\"views\": [{\"defConsumption\": 0,\"nodes\": {\"0\": 239094,\"1\": 111355,\"2\": 239725,\"3\": 110774},\"rcId\": \"memory\",\"id\": \"shareableResource\",\"defCapacity\": 0,\"vms\": {\"11\": 1082,\"12\": 1082,\"13\": 1082,\"14\": 1082,\"15\": 1082,\"0\": 1082,\"1\": 1082,\"2\": 1082,\"3\": 1082,\"4\": 1082,\"5\": 1082,\"6\": 1082,\"7\": 1082,\"8\": 1082,\"9\": 1082,\"10\": 1082}}, {\"defConsumption\": 0,\"nodes\": {\"0\": 63913,\"1\": 61663,\"2\": 67200,\"3\": 61620},\"rcId\": \"cpu\",\"id\": \"shareableResource\",\"defCapacity\": 0,\"vms\": {\"11\": 1271,\"12\": 1263,\"13\": 1257,\"14\": 1243,\"15\": 1253,\"0\": 1252,\"1\": 1238,\"2\": 1236,\"3\": 1253,\"4\": 1257,\"5\": 1244,\"6\": 1255,\"7\": 1251,\"8\": 1244,\"9\": 1240,\"10\": 1246}}, {\"defConsumption\": 0,\"nodes\": {\"0\": 7498,\"1\": 6854,\"2\": 7560,\"3\": 7560},\"rcId\": \"controller_cpu\",\"id\": \"shareableResource\",\"defCapacity\": 0,\"vms\": {\"11\": 431,\"12\": 435,\"13\": 432,\"14\": 423,\"15\": 430,\"0\": 430,\"1\": 420,\"2\": 425,\"3\": 430,\"4\": 429,\"5\": 425,\"6\": 431,\"7\": 429,\"8\": 424,\"9\": 426,\"10\": 428}}]},\"constraints\": [{\"rc\": \"cpu\",\"amount\": 26912,\"nodes\": [0],\"continuous\": false,\"id\": \"resourceCapacity\"}, {\"rc\": \"controller_cpu\",\"amount\": 5608,\"nodes\": [0],\"continuous\": false,\"id\": \"resourceCapacity\"}, {\"rc\": \"cpu\",\"amount\": 25093,\"nodes\": [1],\"continuous\": false,\"id\": \"resourceCapacity\"}, {\"rc\": \"controller_cpu\",\"amount\": 4964,\"nodes\": [1],\"continuous\": false,\"id\": \"resourceCapacity\"}, {\"rc\": \"cpu\",\"amount\": 29568,\"nodes\": [2],\"continuous\": false,\"id\": \"resourceCapacity\"}, {\"rc\": \"controller_cpu\",\"amount\": 5670,\"nodes\": [2],\"continuous\": false,\"id\": \"resourceCapacity\"}, {\"rc\": \"controller_cpu\",\"amount\": 516,\"vm\": 0,\"id\": \"preserve\"}, {\"rc\": \"controller_cpu\",\"amount\": 505,\"vm\": 1,\"id\": \"preserve\"}, {\"rc\": \"controller_cpu\",\"amount\": 510,\"vm\": 2,\"id\": \"preserve\"},{\"rc\": \"controller_cpu\",\"amount\": 516,\"vm\": 3,\"id\": \"preserve\"}, {\"rc\": \"controller_cpu\",\"amount\": 515,\"vm\": 4,\"id\": \"preserve\"}, {\"rc\": \"controller_cpu\",\"amount\": 510,\"vm\": 5,\"id\": \"preserve\"}, {\"rc\": \"controller_cpu\",\"amount\": 517,\"vm\": 6,\"id\": \"preserve\"}, {\"rc\": \"controller_cpu\",\"amount\": 515,\"vm\": 7,\"id\": \"preserve\"}, {\"rc\": \"controller_cpu\",\"amount\": 509,\"vm\": 8,\"id\": \"preserve\"}, {\"rc\": \"controller_cpu\",\"amount\": 511,\"vm\": 9,\"id\": \"preserve\"}, {\"rc\": \"controller_cpu\",\"amount\": 514,\"vm\": 10,\"id\": \"preserve\"}, {\"rc\": \"controller_cpu\",\"amount\": 517,\"vm\": 11,\"id\": \"preserve\"},{\"rc\": \"controller_cpu\",\"amount\": 522,\"vm\": 12,\"id\": \"preserve\"}, {\"rc\": \"controller_cpu\",\"amount\": 519,\"vm\": 13,\"id\": \"preserve\"}, {\"rc\": \"controller_cpu\",\"amount\": 508,\"vm\": 14,\"id\": \"preserve\"}, {\"rc\": \"controller_cpu\",\"amount\": 516,\"vm\": 15,\"id\": \"preserve\"}, {\"rc\": \"cpu\",\"amount\": 25059,\"nodes\": [3],\"continuous\": false,\"id\": \"resourceCapacity\"}, {\"rc\": \"controller_cpu\",\"amount\": 5670,\"nodes\": [3],\"continuous\": false,\"id\": \"resourceCapacity\"}],\"objective\": {\"id\": \"minimizeMigrations\"}}";
        Instance i = JSON.readInstance(new StringReader(buf));
        ChocoScheduler s = new DefaultChocoScheduler();
        ReconfigurationPlan plan = s.solve(i);
        Assert.assertNotNull(plan);
    }
}
