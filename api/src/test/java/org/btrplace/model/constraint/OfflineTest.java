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
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.plan.event.ShutdownVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Unit tests for {@link org.btrplace.model.constraint.Offline}.
 *
 * @author Fabien Hermenier
 */
public class OfflineTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        Node n = mo.newNode();
        Offline o = new Offline(n);
        Assert.assertNotNull(o.getChecker());
        Assert.assertTrue(o.getInvolvedNodes().contains(n));
        Assert.assertTrue(o.getInvolvedVMs().isEmpty());
        Assert.assertNotNull(o.toString());
        Assert.assertTrue(o.setContinuous(false));
        System.out.println(o);
    }

    @Test
    public void testIsSatisfied() {
        Model i = new DefaultModel();
        Mapping c = i.getMapping();
        Node n1 = i.newNode();
        Node n2 = i.newNode();
        c.addOfflineNode(n1);
        Offline o = new Offline(n1);

        Assert.assertEquals(o.isSatisfied(i), true);
        c.addOnlineNode(n2);
        Assert.assertEquals(new Offline(n2).isSatisfied(i), false);
    }

    @Test
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);
        Mapping map = mo.getMapping();

        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));

        Offline off = new Offline(ns.get(0));

        map.addRunningVM(vms.get(0), ns.get(0));

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(off.isSatisfied(plan), false);
        plan.add(new ShutdownNode(ns.get(1), 0, 1));
        plan.add(new ShutdownVM(vms.get(0), ns.get(0), 0, 1));
        Assert.assertEquals(off.isSatisfied(plan), false);
        plan.add(new ShutdownNode(ns.get(0), 1, 2));
        Assert.assertEquals(off.isSatisfied(plan), true);

    }

    @Test
    public void testEquals() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);

        Offline s = new Offline(ns.get(0));

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Offline(ns.get(0)).equals(s));
        Assert.assertEquals(new Offline(ns.get(0)).hashCode(), s.hashCode());
        Assert.assertFalse(new Offline(ns.get(1)).equals(s));
    }

    @Test
    public void testOfflines() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 5);
        List<Offline> c = Offline.newOffline(ns);
        Assert.assertEquals(ns.size(), c.size());
        c.stream().forEach((q) -> {
            Assert.assertTrue(ns.containsAll(q.getInvolvedNodes()));
            Assert.assertFalse(q.isContinuous());
        });
    }
}
