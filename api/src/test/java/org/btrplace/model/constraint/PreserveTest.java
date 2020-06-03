/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.Util;
import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.AllocateEvent;
import org.btrplace.plan.event.MigrateVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Unit tests for {@link Preserve}.
 *
 * @author Fabien Hermenier
 */
public class PreserveTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Preserve p = new Preserve(v, "cpu", 3);
        Assert.assertNotNull(p.getChecker());
        Assert.assertTrue(p.getInvolvedVMs().contains(v));
        Assert.assertTrue(p.getInvolvedNodes().isEmpty());
        Assert.assertEquals(3, p.getAmount());
        Assert.assertEquals("cpu", p.getResource());
        Assert.assertFalse(p.toString().contains("null"));
        Assert.assertFalse(p.isContinuous());
        Assert.assertFalse(p.setContinuous(true));
        System.out.println(p);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsAndHashCode() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Preserve p = new Preserve(v, "cpu", 3);
        Preserve p2 = new Preserve(v, "cpu", 3);
        Assert.assertTrue(p.equals(p));
        Assert.assertTrue(p2.equals(p));
        Assert.assertEquals(p2.hashCode(), p.hashCode());
        Assert.assertFalse(new Preserve(v, "mem", 3).equals(p));
        Assert.assertFalse(new Preserve(v, "cpu", 2).equals(p));
        Assert.assertFalse(new Preserve(mo.newVM(), "cpu", 3).equals(p));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testIsSatisfied() {
        Model m = new DefaultModel();
        List<VM> vms = Util.newVMs(m, 5);
        List<Node> ns = Util.newNodes(m, 5);

        ShareableResource rc = new ShareableResource("cpu", 3, 3);
        Mapping map = m.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addSleepingVM(vms.get(1), ns.get(0));
        map.addRunningVM(vms.get(2), ns.get(0));
        m.attach(rc);
        Preserve p = new Preserve(vms.get(0), "cpu", 3);
        rc.setConsumption(vms.get(0), 3);
        rc.setConsumption(vms.get(1), 1); //Not running so we don't care
        rc.setConsumption(vms.get(2), 3);
        Assert.assertEquals(true, p.isSatisfied(m));

        rc.unset(vms.get(2)); //Set to 3 by default
        Assert.assertEquals(true, p.isSatisfied(m));
        Assert.assertEquals(false, new Preserve(vms.get(2), "mem", 3).isSatisfied(m));

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(m);
        rc.setConsumption(vms.get(1), 1);
        Assert.assertFalse(new Preserve(vms.get(2), "cpu", 4).isSatisfied(plan));
        plan.add(new Allocate(vms.get(2), ns.get(0), "cpu", 7, 5, 7));
        Assert.assertTrue(p.isSatisfied(plan));
        rc.setConsumption(vms.get(0), 1);
        AllocateEvent e = new AllocateEvent(vms.get(0), "cpu", 4);
        Assert.assertFalse(p.isSatisfied(plan));
        MigrateVM mig = new MigrateVM(vms.get(0), ns.get(0), ns.get(1), 0, 3);
        mig.addEvent(Action.Hook.POST, e);
        plan.add(mig);
        Assert.assertTrue(p.isSatisfied(plan));
    }

    @Test
    public void testPreserves() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 5);
        List<Node> ns = Util.newNodes(mo, 5);
        List<Fence> c = Fence.newFence(vms, ns);
        ShareableResource rc = new ShareableResource("foo", 0, 0);
        mo.attach(rc);
        Assert.assertEquals(vms.size(), c.size());
        c.stream().forEach((q) -> {
            Assert.assertTrue(vms.containsAll(q.getInvolvedVMs()));
            Assert.assertEquals(ns, q.getInvolvedNodes());
            Assert.assertFalse(q.isContinuous());
        });
    }
}
