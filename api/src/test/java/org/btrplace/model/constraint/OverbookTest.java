/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
        Assert.assertEquals("foo", o.getResource());
        Assert.assertTrue(o.getInvolvedVMs().isEmpty());
        Assert.assertEquals(1.5, o.getRatio());
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
        Assert.assertEquals(o.isSatisfied(i), true);

        rc.setConsumption(vms.get(0), 4);
        Assert.assertEquals(o.isSatisfied(i), false);

        cfg.addRunningVM(vms.get(0), n1);
        Assert.assertEquals(new Overbook(n1, "cpu", 2).isSatisfied(i), false);

        Overbook o2 = new Overbook(n0, "mem", 2);
        Assert.assertEquals(o2.isSatisfied(i), false);
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
        Assert.assertEquals(o.isSatisfied(p), true);

        p.add(new Allocate(vms.get(0), n0, "cpu", 1, 2, 5));
        Assert.assertEquals(o.isSatisfied(p), true);

        p.add(new Allocate(vms.get(1), n1, "cpu", 5, 2, 5));
        Assert.assertEquals(o.isSatisfied(p), false);

        p.add(new Allocate(vms.get(2), n1, "cpu", 2, 0, 1));
        Assert.assertEquals(o.isSatisfied(p), true);

        p.add(new Allocate(vms.get(3), n1, "cpu", 3, 4, 6));
        Assert.assertEquals(o.isSatisfied(p), false);

        p.add(new ShutdownVM(vms.get(2), n1, 2, 3));

        Assert.assertEquals(o.isSatisfied(p), true);
    }

    @Test
    public void testEquals() {

        Model mo = new DefaultModel();
        Node n = mo.newNode();

        Overbook s = new Overbook(n, "foo", 3);

        Assert.assertTrue(s.equals(s));
        Overbook o2 = new Overbook(n, "foo", 3);
        Assert.assertTrue(o2.equals(s));
        Assert.assertEquals(o2.hashCode(), s.hashCode());
        Assert.assertFalse(new Overbook(n, "bar", 3).equals(s));
        Assert.assertFalse(new Overbook(n, "foo", 2).equals(s));
        Assert.assertFalse(new Overbook(mo.newNode(), "foo", 3).equals(s));
        Assert.assertNotEquals(new Overbook(mo.newNode(), "foo", 3, true), new Overbook(mo.newNode(), "foo", 3, false));
        Assert.assertNotEquals(new Overbook(mo.newNode(), "foo", 3, true).hashCode(), new Overbook(mo.newNode(), "foo", 3, false).hashCode());
    }
}
