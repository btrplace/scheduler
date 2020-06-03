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
import org.btrplace.model.constraint.Fence;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Online;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.Spread;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link CSpread}.
 *
 * @author Fabien Hermenier
 */
public class CSpreadTest {

    @Test
    public void testDiscrete() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        mo.getMapping().on(n1, n2, n3)
                .run(n1, vm1).run(n2, vm2);

        List<SatConstraint> cstr = new ArrayList<>();
        ChocoScheduler cra = new DefaultChocoScheduler();
        Spread s = new Spread(mo.getMapping().getAllVMs());
        s.setContinuous(false);
        cstr.add(s);
        cstr.addAll(Online.newOnline(mo.getMapping().getAllNodes()));
        cstr.add(new Fence(vm1, Collections.singleton(n2)));
        ReconfigurationPlan p = cra.solve(mo, cstr);
        Assert.assertNotNull(p);
        System.err.println(p);
        Mapping res = p.getResult().getMapping();
        Assert.assertEquals(2, p.getSize());
        Assert.assertNotSame(res.getVMLocation(vm1), res.getVMLocation(vm2));
    }

    @Test
    public void testContinuous() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        mo.getMapping().on(n1, n2, n3)
                .run(n1, vm1).run(n2, vm2);

        List<SatConstraint> cstr = new ArrayList<>();
        ChocoScheduler cra = new DefaultChocoScheduler();
        cstr.add(new Spread(mo.getMapping().getAllVMs(), true));
        cstr.addAll(Online.newOnline(mo.getMapping().getAllNodes()));
        cstr.add(new Fence(vm1, Collections.singleton(n2)));
        ReconfigurationPlan p = cra.solve(mo, cstr);
        Assert.assertNotNull(p);
        System.err.println(p);
        Mapping res = p.getResult().getMapping();
        Assert.assertEquals(2, p.getSize());
        Assert.assertNotSame(res.getVMLocation(vm1), res.getVMLocation(vm2));
    }

    @Test
    public void testGetMisplaced() {

        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = mo.getMapping().on(n1, n2)
                .run(n1, vm1, vm3)
                .run(n2, vm2);
        Set<VM> vms = new HashSet<>(Arrays.asList(vm1, vm2));
        Spread s = new Spread(vms);
        CSpread cs = new CSpread(s);

        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        Assert.assertTrue(cs.getMisPlacedVMs(i).isEmpty());
        vms.add(vm3);
        Assert.assertEquals(map.getRunningVMs(n1), cs.getMisPlacedVMs(i));
    }

    /**
     * 2 VMs are already hosted on a same node, check
     * if separation is working in continuous mode
     */
    @Test
    public void testSeparateWithContinuous() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        mo.getMapping().on(n1, n2).run(n1, vm1, vm2);

        List<SatConstraint> cstr = new ArrayList<>();
        ChocoScheduler cra = new DefaultChocoScheduler();
        Spread s = new Spread(mo.getMapping().getAllVMs());
        s.setContinuous(true);
        cstr.add(s);
        cstr.addAll(Online.newOnline(mo.getMapping().getAllNodes()));
        cstr.add(new Fence(vm1, Collections.singleton(n2)));
        ReconfigurationPlan p = cra.solve(mo, cstr);
        Assert.assertNull(p);
    }

    @Test
    public void testIssue48() {
        Model mo = new DefaultModel();
        VM v1 = mo.newVM();
        VM v2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        mo.getMapping().on(n1, n2)
                .ready(v1)
                .run(n1, v2);

        Spread s = new Spread(mo.getMapping().getAllVMs(), true);
        ChocoScheduler sched = new DefaultChocoScheduler();
        Assert.assertNotNull(sched.solve(mo, Arrays.asList(s)));
    }

    @Test
    public void testFoo() {
        String buf = "{\"model\":{\"mapping\":{\"readyVMs\":[],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[4,3,2,1,0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[9,8,7,6,5]},\"2\":{\"sleepingVMs\":[],\"runningVMs\":[14,13,12,11,10]},\"3\":{\"sleepingVMs\":[],\"runningVMs\":[17,16,15]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{}},\"views\":[{\"defConsumption\":0,\"nodes\":{\"0\":48986,\"1\":48986,\"2\":48986,\"3\":48986},\"rcId\":\"memory\",\"id\":\"shareableResource\",\"defCapacity\":0,\"vms\":{\"11\":8661,\"12\":8661,\"13\":8661,\"14\":8661,\"15\":8661,\"16\":8661,\"17\":8661,\"0\":8661,\"1\":8661,\"2\":8661,\"3\":8661,\"4\":8661,\"5\":8661,\"6\":8661,\"7\":8661,\"8\":8661,\"9\":8665,\"10\":8661}},{\"defConsumption\":0,\"nodes\":{\"0\":47040,\"1\":47040,\"2\":47040,\"3\":47098},\"rcId\":\"cpu\",\"id\":\"shareableResource\",\"defCapacity\":0,\"vms\":{\"11\":6,\"12\":6,\"13\":11,\"14\":12,\"15\":5,\"16\":5,\"17\":5,\"0\":5029,\"1\":8956,\"2\":6009,\"3\":9856,\"4\":27,\"5\":6,\"6\":5,\"7\":5,\"8\":6,\"9\":4,\"10\":6}},{\"defConsumption\":0,\"nodes\":{\"0\":1727,\"1\":1705,\"2\":1727,\"3\":1673},\"rcId\":\"controller_cpu\",\"id\":\"shareableResource\",\"defCapacity\":0,\"vms\":{\"11\":42,\"12\":22,\"13\":9,\"14\":28,\"15\":30,\"16\":29,\"17\":31,\"0\":28,\"1\":56,\"2\":43,\"3\":49,\"4\":26,\"5\":19,\"6\":38,\"7\":25,\"8\":35,\"9\":57,\"10\":39}}]},\"constraints\":[{\"rc\":\"cpu\",\"amount\":26927,\"nodes\":[0],\"continuous\":false,\"id\":\"resourceCapacity\"},{\"rc\":\"memory\",\"amount\":48986,\"nodes\":[0],\"continuous\":false,\"id\":\"resourceCapacity\"},{\"rc\":\"controller_cpu\",\"amount\":1468,\"nodes\":[0],\"continuous\":false,\"id\":\"resourceCapacity\"},{\"rc\":\"cpu\",\"amount\":6035,\"vm\":0,\"id\":\"preserve\"},{\"rc\":\"cpu\",\"amount\":10747,\"vm\":1,\"id\":\"preserve\"},{\"rc\":\"cpu\",\"amount\":7211,\"vm\":2,\"id\":\"preserve\"},{\"rc\":\"cpu\",\"amount\":11827,\"vm\":3,\"id\":\"preserve\"},{\"rc\":\"cpu\",\"amount\":32,\"vm\":4,\"id\":\"preserve\"},{\"rc\":\"cpu\",\"amount\":26927,\"nodes\":[1],\"continuous\":false,\"id\":\"resourceCapacity\"},{\"rc\":\"memory\",\"amount\":48986,\"nodes\":[1],\"continuous\":false,\"id\":\"resourceCapacity\"},{\"rc\":\"controller_cpu\",\"amount\":1449,\"nodes\":[1],\"continuous\":false,\"id\":\"resourceCapacity\"},{\"rc\":\"cpu\",\"amount\":26927,\"nodes\":[2],\"continuous\":false,\"id\":\"resourceCapacity\"},{\"rc\":\"memory\",\"amount\":48986,\"nodes\":[2],\"continuous\":false,\"id\":\"resourceCapacity\"},{\"rc\":\"controller_cpu\",\"amount\":1468,\"nodes\":[2],\"continuous\":false,\"id\":\"resourceCapacity\"},{\"rc\":\"cpu\",\"amount\":26961,\"nodes\":[3],\"continuous\":false,\"id\":\"resourceCapacity\"},{\"rc\":\"memory\",\"amount\":48986,\"nodes\":[3],\"continuous\":false,\"id\":\"resourceCapacity\"},{\"rc\":\"controller_cpu\",\"amount\":1422,\"nodes\":[3],\"continuous\":false,\"id\":\"resourceCapacity\"},{\"continuous\":true,\"id\":\"spread\",\"vms\":[17,3,9,13]}],\"objective\":{\"id\":\"minimizeMTTR\"}}";
        Instance i = JSON.readInstance(new StringReader(buf));
        ChocoScheduler s = new DefaultChocoScheduler();
        //s.doOptimize(false);
        s.setMaxEnd(3);
        s.doRepair(true);
        s.solve(i);
    }
}
