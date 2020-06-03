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
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ShutdownVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Unit tests for {@link Quarantine}.
 *
 * @author Fabien Hermenier
 */
public class QuarantineTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        Node n = mo.newNode();
        Quarantine q = new Quarantine(n);
        Assert.assertNotNull(q.getChecker());
        Assert.assertTrue(q.getInvolvedVMs().isEmpty());
        Assert.assertEquals(q.getInvolvedNodes().iterator().next(), n);
        Assert.assertTrue(q.isContinuous());
        Assert.assertFalse(q.setContinuous(false));
        Assert.assertTrue(q.setContinuous(true));
        Assert.assertFalse(q.toString().contains("null"));
//        Assert.assertEquals(q.isSatisfied(new DefaultModel()), SatConstraint.Sat.UNDEFINED);
        System.out.println(q);
    }

    @Test
    public void testEqualsHashCode() {
        Model mo = new DefaultModel();
        Node n = mo.newNode();
        Quarantine q = new Quarantine(n);
        Assert.assertTrue(q.equals(q));
        Assert.assertTrue(q.equals(new Quarantine(n)));
        Assert.assertEquals(q.hashCode(), new Quarantine(n).hashCode());
        Assert.assertFalse(q.equals(new Quarantine(mo.newNode())));
    }

    @Test
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 5);
        List<Node> ns = Util.newNodes(mo, 5);
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(1));
        map.addReadyVM(vms.get(2));
        map.addRunningVM(vms.get(3), ns.get(2));

        Quarantine q = new Quarantine(ns.get(0));

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(q.isSatisfied(plan), true);
        plan.add(new ShutdownVM(vms.get(1), ns.get(1), 1, 2));
        Assert.assertEquals(q.isSatisfied(plan), true);

        plan.add(new BootVM(vms.get(2), ns.get(0), 0, 1));
        Assert.assertEquals(q.isSatisfied(plan), false);

        plan = new DefaultReconfigurationPlan(mo);
        plan.add(new BootVM(vms.get(2), ns.get(2), 0, 1));
        Assert.assertEquals(new Quarantine(ns.get(1)).isSatisfied(plan), true);
        plan.add(new MigrateVM(vms.get(3), ns.get(2), ns.get(1), 0, 1));
        Assert.assertEquals(new Quarantine(ns.get(1)).isSatisfied(plan), false);

        plan = new DefaultReconfigurationPlan(mo);
        plan.add(new MigrateVM(vms.get(1), ns.get(1), ns.get(0), 0, 1));
        Assert.assertEquals(q.isSatisfied(plan), false);
    }

    @Test
    public void testQuarantines() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 5);
        List<Quarantine> qs = Quarantine.newQuarantine(ns);
        Assert.assertEquals(qs.size(), ns.size());
        qs.stream().forEach((q) -> {
            Assert.assertTrue(ns.containsAll(q.getInvolvedNodes()));
            Assert.assertTrue(q.isContinuous());
        });
    }
}
