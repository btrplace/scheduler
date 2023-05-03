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
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.ShutdownVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Unit tests for {@link Overbook}.
 *
 * @author Fabien Hermenier
 */
public class OverbookTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        Node n = mo.newNode();

        Overbook o = new Overbook(n, "foo", 1.5);
        Assert.assertNotNull(o.getChecker());
        Assert.assertEquals(n, o.getInvolvedNodes().iterator().next());
        Assert.assertEquals(o.getResource(), "foo");
        Assert.assertTrue(o.getInvolvedVMs().isEmpty());
        Assert.assertEquals(o.getRatio(), 1.5);
        Assert.assertNotNull(o.toString());
        Assert.assertTrue(o.isContinuous());
        Assert.assertTrue(o.setContinuous(false));
        Assert.assertFalse(o.isContinuous());
        System.out.println(o);

        o = new Overbook(n, "foo", 1.5, true);
        Assert.assertTrue(o.isContinuous());
    }

    @Test
    public void testDiscreteIsSatisfied() {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();
        List<VM> vms = Util.newVMs(mo, 10);

        Model i = new DefaultModel();
        Mapping cfg = i.getMapping();
        cfg.addOnlineNode(n0);
        cfg.addOnlineNode(n1);

        ShareableResource rc = new ShareableResource("cpu");
        rc.setCapacity(n0, 1);
        rc.setCapacity(n1, 4);

        rc.setConsumption(vms.get(0), 2);
        rc.setConsumption(vms.get(1), 2);
        rc.setConsumption(vms.get(2), 4);

        cfg.addRunningVM(vms.get(0), n0);
        cfg.addRunningVM(vms.get(1), n1);
        cfg.addRunningVM(vms.get(2), n1);
        cfg.addRunningVM(vms.get(3), n1);

        i.attach(rc);

        Overbook o = new Overbook(n0, "cpu", 2);
        Assert.assertTrue(o.isSatisfied(i));

        rc.setConsumption(vms.get(0), 4);
        Assert.assertFalse(o.isSatisfied(i));

        cfg.addRunningVM(vms.get(0), n1);
        Assert.assertFalse(new Overbook(n1, "cpu", 2).isSatisfied(i));

        Overbook o2 = new Overbook(n0, "mem", 2);
        Assert.assertFalse(o2.isSatisfied(i));
    }

    @Test
    public void testContinuousIsSatisfied() {

        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 10);

        Node n0 = mo.newNode();
        Node n1 = mo.newNode();

        Model i = new DefaultModel();
        Mapping cfg = i.getMapping();
        cfg.addOnlineNode(n0);
        cfg.addOnlineNode(n1);

        ShareableResource rc = new ShareableResource("cpu");
        rc.setCapacity(n0, 1);
        rc.setCapacity(n1, 4);

        rc.setConsumption(vms.get(0), 2);
        rc.setConsumption(vms.get(1), 2);
        rc.setConsumption(vms.get(2), 4);

        cfg.addRunningVM(vms.get(0), n0);
        cfg.addRunningVM(vms.get(1), n1);
        cfg.addRunningVM(vms.get(2), n1);
        cfg.addRunningVM(vms.get(3), n1);

        i.attach(rc);

        Overbook o = new Overbook(n1, "cpu", 2);
        o.setContinuous(true);
        ReconfigurationPlan p = new DefaultReconfigurationPlan(i);
        Assert.assertTrue(o.isSatisfied(p));

        p.add(new Allocate(vms.get(0), n0, "cpu", 1, 2, 5));
        Assert.assertTrue(o.isSatisfied(p));

        p.add(new Allocate(vms.get(1), n1, "cpu", 5, 2, 5));
        Assert.assertFalse(o.isSatisfied(p));

        p.add(new Allocate(vms.get(2), n1, "cpu", 2, 0, 1));
        Assert.assertTrue(o.isSatisfied(p));

        p.add(new Allocate(vms.get(3), n1, "cpu", 3, 4, 6));
        Assert.assertFalse(o.isSatisfied(p));

        p.add(new ShutdownVM(vms.get(2), n1, 2, 3));

        Assert.assertTrue(o.isSatisfied(p));
    }

    @Test
    public void testEquals() {

        Model mo = new DefaultModel();
        Node n = mo.newNode();

        Overbook s = new Overbook(n, "foo", 3);

        Assert.assertEquals(s, s);
        Overbook o2 = new Overbook(n, "foo", 3);
        Assert.assertEquals(s, o2);
        Assert.assertEquals(o2.hashCode(), s.hashCode());
        Assert.assertNotEquals(s, new Overbook(n, "bar", 3));
        Assert.assertNotEquals(s, new Overbook(n, "foo", 2));
        Assert.assertNotEquals(s, new Overbook(mo.newNode(), "foo", 3));
        Assert.assertNotEquals(new Overbook(mo.newNode(), "foo", 3, true), new Overbook(mo.newNode(), "foo", 3, false));
        Assert.assertNotEquals(new Overbook(mo.newNode(), "foo", 3, true).hashCode(), new Overbook(mo.newNode(), "foo", 3, false).hashCode());
    }
}
