/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.*;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link ResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class ResourceCapacityTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        Set<Node> s = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        ResourceCapacity c = new ResourceCapacity(s, "foo", 3);
        Assert.assertNotNull(c.getChecker());
        Assert.assertEquals(s, c.getInvolvedNodes());
        Assert.assertEquals(c.getResource(), "foo");
        Assert.assertEquals(c.getAmount(), 3);
        Assert.assertTrue(c.getInvolvedVMs().isEmpty());
        Assert.assertFalse(c.toString().contains("null"));
        Assert.assertFalse(c.isContinuous());
        Assert.assertTrue(c.setContinuous(true));
        Assert.assertTrue(c.isContinuous());

        c = new ResourceCapacity(s, "foo", 3, true);
        Assert.assertTrue(c.isContinuous());

        System.out.println(c);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsAndHashCode() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        Set<Node> s = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        ResourceCapacity c = new ResourceCapacity(s, "foo", 3);
        ResourceCapacity c2 = new ResourceCapacity(s, "foo", 3);
        Assert.assertEquals(c, c);
        Assert.assertEquals(c2, c);
        Assert.assertEquals(c.hashCode(), c2.hashCode());

        Assert.assertNotEquals(new ResourceCapacity(s, "bar", 3), c);
        Assert.assertNotEquals(new ResourceCapacity(s, "foo", 2), c);
        Assert.assertNotEquals(new ResourceCapacity(new HashSet<>(), "foo", 3), c);
        Assert.assertNotEquals(new ResourceCapacity(s, "f", 3, true), new ResourceCapacity(s, "f", 3, false));
        Assert.assertNotEquals(new ResourceCapacity(s, "f", 3, true).hashCode(), new ResourceCapacity(s, "f", 3, false).hashCode());
    }

    @Test
    public void testDiscreteIsSatisfied() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));

        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(0));
        map.addRunningVM(vms.get(2), ns.get(1));
        map.addRunningVM(vms.get(3), ns.get(2));

        ShareableResource rc = new ShareableResource("foo", 1, 1);
        rc.setConsumption(vms.get(1), 2);
        mo.attach(rc);
        Set<Node> nodes = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        ResourceCapacity cc = new ResourceCapacity(nodes, "foo", 4);
        Assert.assertTrue(cc.isSatisfied(mo));
        Assert.assertFalse(new ResourceCapacity(nodes, "bar", 100).isSatisfied(mo));

        rc.setConsumption(vms.get(0), 3);
        Assert.assertFalse(cc.isSatisfied(mo));
        map.addSleepingVM(vms.get(1), ns.get(0));
        map.addSleepingVM(vms.get(2), ns.get(0));
        Assert.assertTrue(cc.isSatisfied(mo));
    }

    @Test
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));

        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(0));
        map.addRunningVM(vms.get(2), ns.get(1));
        map.addRunningVM(vms.get(3), ns.get(2));
        map.addReadyVM(vms.get(4));
        ShareableResource rc = new ShareableResource("foo", 1, 1);
        mo.attach(rc);

        Set<Node> nodes = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        ResourceCapacity cc = new ResourceCapacity(nodes, "foo", 4, true);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertTrue(cc.isSatisfied(plan));
        //3/4
        MigrateVM m = new MigrateVM(vms.get(3), ns.get(2), ns.get(1), 0, 1);
        m.addEvent(Action.Hook.POST, new AllocateEvent(vms.get(3), "foo", 2));
        plan.add(m);
        //5/4
        plan.add(new ShutdownVM(vms.get(2), ns.get(1), 1, 2));
        //4/4
        plan.add(new BootVM(vms.get(4), ns.get(2), 2, 3));
        //4/4
        plan.add(new Allocate(vms.get(1), ns.get(0), "foo", 2, 2, 3));
        //5/4
        plan.add(new MigrateVM(vms.get(0), ns.get(0), ns.get(2), 3, 4));
        System.out.println(plan);
        Assert.assertTrue(cc.isSatisfied(plan));

    }
}
