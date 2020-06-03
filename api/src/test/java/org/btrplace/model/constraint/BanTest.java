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
import org.btrplace.plan.event.MigrateVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link Ban}.
 *
 * @author Fabien Hermenier
 */
public class BanTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        Set<Node> nodes = new HashSet<>(Collections.singletonList(mo.newNode()));
        VM v = mo.newVM();
        Ban b = new Ban(v, nodes);
        Assert.assertTrue(b.getInvolvedVMs().contains(v));
        Assert.assertEquals(nodes, b.getInvolvedNodes());
        Assert.assertFalse(b.toString().contains("null"));
        Assert.assertTrue(b.setContinuous(true));
        Assert.assertTrue(b.isContinuous());
        Assert.assertNotNull(b.getChecker());
        System.out.println(b);
      Ban b2 = new Ban(v, nodes.iterator().next());
      Assert.assertEquals(v, b2.getInvolvedVMs().iterator().next());
      Assert.assertEquals(nodes.iterator().next(), b2.getInvolvedNodes().iterator().next());
    }

    @Test
    public void testIsSatisfied() {

        Model m = new DefaultModel();
        List<VM> vms = Util.newVMs(m, 10);
        List<Node> ns = Util.newNodes(m, 10);
        Mapping map = m.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));

        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(1));
        map.addRunningVM(vms.get(2), ns.get(2));
        Set<Node> nodes = new HashSet<>(Collections.singletonList(ns.get(0)));

        Ban b = new Ban(vms.get(2), nodes);
        Assert.assertEquals(b.isSatisfied(m), true);
        map.addRunningVM(vms.get(2), ns.get(0));
        Assert.assertEquals(new Ban(vms.get(2), nodes).isSatisfied(m), false);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(m);
        plan.add(new MigrateVM(vms.get(0), ns.get(0), ns.get(1), 0, 3));
        plan.add(new MigrateVM(vms.get(0), ns.get(1), ns.get(0), 3, 6));
        plan.add(new MigrateVM(vms.get(1), ns.get(1), ns.get(2), 3, 6));
        Assert.assertEquals(b.isSatisfied(plan), false);
    }

    @Test
    public void testEquals() {
        Model m = new DefaultModel();
        VM v = m.newVM();
        List<Node> ns = Util.newNodes(m, 10);

        Set<Node> nodes = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));

        Ban b = new Ban(v, nodes);
        Assert.assertTrue(b.equals(b));
        Assert.assertTrue(new Ban(v, nodes).equals(b));
        Assert.assertEquals(new Ban(v, nodes).hashCode(), b.hashCode());
        Assert.assertNotEquals(new Ban(m.newVM(), nodes), b);
    }

    @Test
    public void testBans() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 5);
        List<Node> ns = Util.newNodes(mo, 5);
        List<Ban> c = Ban.newBan(vms, ns);
        Assert.assertEquals(vms.size(), c.size());
        c.stream().forEach((q) -> {
            Assert.assertTrue(vms.containsAll(q.getInvolvedVMs()));
            Assert.assertEquals(ns, q.getInvolvedNodes());
            Assert.assertFalse(q.isContinuous());
        });
    }
}
