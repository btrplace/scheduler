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
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.ShutdownVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link RunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class RunningCapacityTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        Set<Node> s = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        RunningCapacity c = new RunningCapacity(s, 3);
        Assert.assertNotNull(c.getChecker());
        Assert.assertEquals(s, c.getInvolvedNodes());
        Assert.assertEquals(3, c.getAmount());
        Assert.assertTrue(c.getInvolvedVMs().isEmpty());
        Assert.assertFalse(c.toString().contains("null"));

        Assert.assertFalse(c.isContinuous());
        Assert.assertTrue(c.setContinuous(true));
        Assert.assertTrue(c.isContinuous());
        System.out.println(c);

        c = new RunningCapacity(s, 3, true);
        Assert.assertTrue(c.isContinuous());

    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsAndHashCode() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);

        Set<Node> s = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        RunningCapacity c = new RunningCapacity(s, 3);
        RunningCapacity c2 = new RunningCapacity(s, 3);
        Assert.assertTrue(c.equals(c));
        Assert.assertTrue(c.equals(c2));
        Assert.assertEquals(c.hashCode(), c2.hashCode());

        Assert.assertFalse(c.equals(new RunningCapacity(s, 2)));
        Assert.assertFalse(c.equals(new RunningCapacity(new HashSet<>(), 3)));
        Assert.assertNotEquals(new RunningCapacity(s, 2, true), new RunningCapacity(s, 2, false));
        Assert.assertNotEquals(new RunningCapacity(s, 2, true).hashCode(), new RunningCapacity(s, 2, false).hashCode());
    }

    @Test
    public void testDiscreteIsSatisfied() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);
        Mapping m = mo.getMapping();
        m.addOnlineNode(ns.get(0));
        m.addOnlineNode(ns.get(1));
        m.addRunningVM(vms.get(0), ns.get(0));
        m.addReadyVM(vms.get(1));
        m.addRunningVM(vms.get(2), ns.get(1));
        m.addReadyVM(vms.get(3));

        RunningCapacity c = new RunningCapacity(m.getAllNodes(), 2);
        c.setContinuous(false);
        Assert.assertEquals(c.isSatisfied(mo), true);
        m.addRunningVM(vms.get(1), ns.get(1));
        Assert.assertEquals(c.isSatisfied(mo), false);
    }

    @Test
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);

        m.addOnlineNode(ns.get(0));
        m.addOnlineNode(ns.get(1));
        m.addRunningVM(vms.get(0), ns.get(0));
        m.addReadyVM(vms.get(1));

        m.addRunningVM(vms.get(2), ns.get(1));
        m.addReadyVM(vms.get(3));

        RunningCapacity c = new RunningCapacity(m.getAllNodes(), 2);
        c.setContinuous(true);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(c.isSatisfied(plan), true);
        plan.add(new BootVM(vms.get(3), ns.get(1), 2, 4));
        Assert.assertEquals(c.isSatisfied(plan), false);
        plan.add(new ShutdownVM(vms.get(0), ns.get(0), 0, 1));
        Assert.assertEquals(c.isSatisfied(plan), true);


    }
}
