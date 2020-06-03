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

import java.util.List;

/**
 * Unit tests for {@link org.btrplace.model.constraint.Root}.
 *
 * @author Fabien Hermenier
 */
public class RootTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();

        Root s = new Root(v);
        Assert.assertNotNull(s.getChecker());
        Assert.assertEquals(s.getInvolvedVMs().iterator().next(), v);
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertNotNull(s.toString());
        System.out.println(s);
        Assert.assertTrue(s.isContinuous());
        Assert.assertFalse(s.setContinuous(false));
        Assert.assertTrue(s.isContinuous());
        Assert.assertTrue(s.setContinuous(true));
        Assert.assertTrue(s.isContinuous());
    }

    @Test
    public void testEquals() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();

        Root s = new Root(v);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Root(v).equals(s));
        Assert.assertEquals(s.hashCode(), new Root(v).hashCode());
        Assert.assertFalse(new Root(mo.newVM()).equals(s));
    }

    @Test
    public void testIsSatisfied() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        VM v = mo.newVM();
        map.addReadyVM(v);
        Root o = new Root(v);

        Assert.assertEquals(o.isSatisfied(mo), true);
        map.clear();
        Assert.assertEquals(o.isSatisfied(mo), true);
    }

    @Test
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        List<Node> ns = Util.newNodes(mo, 3);
        VM vm1 = mo.newVM();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addRunningVM(vm1, ns.get(0));
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        Root r = new Root(vm1);
        Assert.assertEquals(r.isSatisfied(p), true);
        p.add(new MigrateVM(vm1, ns.get(0), ns.get(1), 1, 2));
        Assert.assertEquals(r.isSatisfied(p), false);
    }

    @Test
    public void testRoots() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 5);
        List<Root> rs = Root.newRoots(vms);
        Assert.assertEquals(rs.size(), vms.size());
        rs.stream().forEach((q) -> {
            Assert.assertTrue(vms.containsAll(q.getInvolvedVMs()));
            Assert.assertTrue(q.isContinuous());
        });
    }
}
