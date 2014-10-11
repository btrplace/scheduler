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
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.MigrateVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link Gather}.
 *
 * @author Fabien Hermenier
 */
public class GatherTest {

    @Test
    public void testInstantiate() {
        Model mo = new DefaultModel();
        Set<VM> s = new HashSet<>(Arrays.asList(mo.newVM(), mo.newVM()));
        Gather g = new Gather(s);
        Assert.assertNotNull(g.getChecker());
        Assert.assertTrue(g.getInvolvedNodes().isEmpty());
        Assert.assertEquals(g.getInvolvedVMs(), s);
        Assert.assertFalse(g.toString().contains("null"));
        Assert.assertFalse(g.isContinuous());
        Assert.assertTrue(g.setContinuous(true));
        Assert.assertTrue(g.isContinuous());
        System.out.println(g);

        g = new Gather(s, true);
        Assert.assertTrue(g.isContinuous());
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testEqualsHashCode() {
        Model mo = new DefaultModel();
        VM vm = mo.newVM();
        Set<VM> s = new HashSet<>(Arrays.asList(vm, mo.newVM()));
        Gather g = new Gather(s);
        Assert.assertTrue(g.equals(g));
        Assert.assertFalse(g.equals(new Object()));
        Gather g2 = new Gather(new HashSet<>(s));
        Assert.assertTrue(g2.equals(g));
        Assert.assertEquals(g2.hashCode(), g.hashCode());
        s.remove(vm);
        Assert.assertFalse(g2.equals(g));
        Assert.assertFalse(new Gather(s, false).equals(new Gather(s, true)));
        Assert.assertNotEquals(new Gather(s, false).hashCode(), new Gather(s, true).hashCode());
    }

    @Test
    public void testDiscreteIsSatisfied() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);

        Set<VM> s = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1)));
        Gather g = new Gather(s);

        Mapping map = mo.getMapping();


        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addReadyVM(vms.get(1));

        Assert.assertEquals(g.isSatisfied(mo), true);
        map.addRunningVM(vms.get(1), ns.get(0));
        Assert.assertEquals(g.isSatisfied(mo), true);
        map.addRunningVM(vms.get(1), ns.get(1));
        Assert.assertEquals(g.isSatisfied(mo), false);
    }

    @Test(dependsOnMethods = {"testDiscreteIsSatisfied"})
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);


        Set<VM> s = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1)));
        Gather g = new Gather(s);
        g.setContinuous(true);
        Mapping map = mo.getMapping();

        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addReadyVM(vms.get(1));
        map.addRunningVM(vms.get(1), ns.get(1));
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(g.isSatisfied(plan), false);

        map.addReadyVM(vms.get(1));
        Assert.assertEquals(g.isSatisfied(plan), true);
        plan.add(new BootVM(vms.get(1), ns.get(0), 0, 1));
        Assert.assertEquals(g.isSatisfied(plan), true);

        map.addRunningVM(vms.get(1), ns.get(0));
        plan = new DefaultReconfigurationPlan(mo);
        plan.add(new MigrateVM(vms.get(1), ns.get(0), ns.get(1), 0, 1));
        plan.add(new MigrateVM(vms.get(0), ns.get(0), ns.get(1), 0, 1));
        Assert.assertEquals(g.isSatisfied(plan), false);
    }
}
