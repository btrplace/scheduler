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
import btrplace.plan.event.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link CumulatedResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CumulatedResourceCapacityTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        Set<Node> s = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        CumulatedResourceCapacity c = new CumulatedResourceCapacity(s, "foo", 3);
        Assert.assertNotNull(c.getChecker());
        Assert.assertEquals(s, c.getInvolvedNodes());
        Assert.assertEquals("foo", c.getResource());
        Assert.assertEquals(3, c.getAmount());
        Assert.assertTrue(c.getInvolvedVMs().isEmpty());
        Assert.assertFalse(c.toString().contains("null"));
        Assert.assertFalse(c.isContinuous());
        Assert.assertTrue(c.setContinuous(true));
        Assert.assertTrue(c.isContinuous());

        c = new CumulatedResourceCapacity(s, "foo", 3, true);
        Assert.assertTrue(c.isContinuous());

        System.out.println(c);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsAndHashCode() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        Set<Node> s = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        CumulatedResourceCapacity c = new CumulatedResourceCapacity(s, "foo", 3);
        CumulatedResourceCapacity c2 = new CumulatedResourceCapacity(s, "foo", 3);
        Assert.assertTrue(c.equals(c));
        Assert.assertTrue(c.equals(c2));
        Assert.assertEquals(c.hashCode(), c2.hashCode());

        Assert.assertFalse(c.equals(new CumulatedResourceCapacity(s, "bar", 3)));
        Assert.assertFalse(c.equals(new CumulatedResourceCapacity(s, "foo", 2)));
        Assert.assertFalse(c.equals(new CumulatedResourceCapacity(new HashSet<Node>(), "foo", 3)));
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
        CumulatedResourceCapacity cc = new CumulatedResourceCapacity(nodes, "foo", 4);
        Assert.assertEquals(cc.isSatisfied(mo), true);
        Assert.assertEquals(new CumulatedResourceCapacity(nodes, "bar", 100).isSatisfied(mo), false);

        rc.setConsumption(vms.get(0), 3);
        Assert.assertEquals(cc.isSatisfied(mo), false);
        map.addSleepingVM(vms.get(1), ns.get(0));
        map.addSleepingVM(vms.get(2), ns.get(0));
        Assert.assertEquals(cc.isSatisfied(mo), true);
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
        CumulatedResourceCapacity cc = new CumulatedResourceCapacity(nodes, "foo", 4, true);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(cc.isSatisfied(plan), true);
        //3/4
        MigrateVM m = new MigrateVM(vms.get(3), ns.get(2), ns.get(1), 0, 1);
        m.addEvent(Action.Hook.post, new AllocateEvent(vms.get(3), "foo", 2));
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
        Assert.assertEquals(cc.isSatisfied(plan), true);

    }
}
