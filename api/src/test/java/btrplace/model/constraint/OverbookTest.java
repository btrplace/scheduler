/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.model.constraint;

import btrplace.model.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Allocate;
import btrplace.plan.event.ShutdownVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link Overbook}.
 *
 * @author Fabien Hermenier
 */
public class OverbookTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);

        Set<Node> s = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        Overbook o = new Overbook(s, "foo", 1.5);
        Assert.assertNotNull(o.getChecker());
        Assert.assertEquals(s, o.getInvolvedNodes());
        Assert.assertEquals("foo", o.getResource());
        Assert.assertTrue(o.getInvolvedVMs().isEmpty());
        Assert.assertEquals(1.5, o.getRatio());
        Assert.assertNotNull(o.toString());
        Assert.assertTrue(o.isContinuous());
        Assert.assertTrue(o.setContinuous(false));
        Assert.assertFalse(o.isContinuous());
        System.out.println(o);

        o = new Overbook(s, "foo", 1.5, true);
        Assert.assertTrue(o.isContinuous());
    }

    @Test
    public void testDiscreteIsSatisfied() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);
        Set<Node> s = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));

        Model i = new DefaultModel();
        Mapping cfg = i.getMapping();
        cfg.addOnlineNode(ns.get(0));
        cfg.addOnlineNode(ns.get(1));

        ShareableResource rc = new ShareableResource("cpu");
        rc.setCapacity(ns.get(0), 1);
        rc.setCapacity(ns.get(1), 4);

        rc.setConsumption(vms.get(0), 2);
        rc.setConsumption(vms.get(1), 2);
        rc.setConsumption(vms.get(2), 4);

        cfg.addRunningVM(vms.get(0), ns.get(0));
        cfg.addRunningVM(vms.get(1), ns.get(1));
        cfg.addRunningVM(vms.get(2), ns.get(1));
        cfg.addRunningVM(vms.get(3), ns.get(1));

        i.attach(rc);

        Overbook o = new Overbook(s, "cpu", 2);
        Assert.assertEquals(o.isSatisfied(i), true);

        rc.setConsumption(vms.get(0), 4);
        Assert.assertEquals(o.isSatisfied(i), false);

        cfg.addRunningVM(vms.get(0), ns.get(1));
        Assert.assertEquals(o.isSatisfied(i), false);

        Overbook o2 = new Overbook(s, "mem", 2);
        Assert.assertEquals(o2.isSatisfied(i), false);
    }

    @Test
    public void testContinuousIsSatisfied() {

        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);

        Set<Node> s = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));

        Model i = new DefaultModel();
        Mapping cfg = i.getMapping();
        cfg.addOnlineNode(ns.get(0));
        cfg.addOnlineNode(ns.get(1));

        ShareableResource rc = new ShareableResource("cpu");
        rc.setCapacity(ns.get(0), 1);
        rc.setCapacity(ns.get(1), 4);

        rc.setConsumption(vms.get(0), 2);
        rc.setConsumption(vms.get(1), 2);
        rc.setConsumption(vms.get(2), 4);

        cfg.addRunningVM(vms.get(0), ns.get(0));
        cfg.addRunningVM(vms.get(1), ns.get(1));
        cfg.addRunningVM(vms.get(2), ns.get(1));
        cfg.addRunningVM(vms.get(3), ns.get(1));

        i.attach(rc);

        Overbook o = new Overbook(s, "cpu", 2);
        o.setContinuous(true);
        ReconfigurationPlan p = new DefaultReconfigurationPlan(i);
        Assert.assertEquals(o.isSatisfied(p), true);

        p.add(new Allocate(vms.get(0), ns.get(0), "cpu", 1, 2, 5));
        Assert.assertEquals(o.isSatisfied(p), true);

        p.add(new Allocate(vms.get(1), ns.get(1), "cpu", 5, 2, 5));
        Assert.assertEquals(o.isSatisfied(p), false);

        p.add(new Allocate(vms.get(2), ns.get(1), "cpu", 2, 0, 1));
        Assert.assertEquals(o.isSatisfied(p), true);

        p.add(new Allocate(vms.get(3), ns.get(1), "cpu", 3, 4, 6));
        Assert.assertEquals(o.isSatisfied(p), false);

        p.add(new ShutdownVM(vms.get(2), ns.get(1), 2, 3));

        Assert.assertEquals(o.isSatisfied(p), true);
    }

    @Test
    public void testEquals() {

        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);

        Set<Node> x = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        Overbook s = new Overbook(x, "foo", 3);

        Assert.assertTrue(s.equals(s));
        Overbook o2 = new Overbook(x, "foo", 3);
        Assert.assertTrue(o2.equals(s));
        Assert.assertEquals(o2.hashCode(), s.hashCode());
        Assert.assertFalse(new Overbook(x, "bar", 3).equals(s));
        Assert.assertFalse(new Overbook(x, "foo", 2).equals(s));
        x = new HashSet<>(Arrays.asList(ns.get(2)));
        Assert.assertFalse(new Overbook(x, "foo", 3).equals(s));
    }
}
