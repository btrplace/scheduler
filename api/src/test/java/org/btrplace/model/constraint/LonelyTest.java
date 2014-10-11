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
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ShutdownVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link Lonely}.
 *
 * @author Fabien Hermenier
 */
public class LonelyTest {

    @Test
    public void testInstantiation() {
        Model i = new DefaultModel();
        Set<VM> s = new HashSet<>(Arrays.asList(i.newVM(), i.newVM(), i.newVM()));

        Lonely l = new Lonely(s);
        Assert.assertNotNull(l.getChecker());
        Assert.assertFalse(l.toString().contains("null"));
        Assert.assertEquals(l.getInvolvedVMs(), s);
        Assert.assertTrue(l.getInvolvedNodes().isEmpty());
        Assert.assertFalse(l.isContinuous());
        Assert.assertTrue(l.setContinuous(true));
        Assert.assertTrue(l.setContinuous(false));
        System.out.println(l);

        l = new Lonely(s, true);
        Assert.assertTrue(l.isContinuous());
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsHashCode() {
        Model i = new DefaultModel();
        Set<VM> s = new HashSet<>(Arrays.asList(i.newVM(), i.newVM(), i.newVM()));
        Lonely l = new Lonely(s);
        Assert.assertTrue(l.equals(l));
        Assert.assertTrue(l.equals(new Lonely(new HashSet<>(s))));
        Assert.assertEquals(l.hashCode(), new Lonely(new HashSet<>(s)).hashCode());
        Assert.assertFalse(l.equals(new Lonely(new HashSet<VM>())));
        Assert.assertNotEquals(new Lonely(s, true), new Lonely(s, false));
        Assert.assertNotEquals(new Lonely(s, true).hashCode(), new Lonely(s, false).hashCode());
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 10);
        List<Node> ns = Util.newNodes(mo, 10);
        Set<VM> s = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1)));

        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));

        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(0));
        map.addRunningVM(vms.get(2), ns.get(1));
        map.addRunningVM(vms.get(3), ns.get(1));

        Lonely l = new Lonely(s, true);

        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(l.isSatisfied(p), true);
        p.add(new MigrateVM(vms.get(1), ns.get(0), ns.get(1), 2, 4));

        Assert.assertEquals(l.isSatisfied(p), false);
        p.add(new ShutdownVM(vms.get(2), ns.get(1), 0, 1));
        Assert.assertEquals(l.isSatisfied(p), false);
        p.add(new MigrateVM(vms.get(3), ns.get(1), ns.get(2), 1, 2));
        Assert.assertEquals(l.isSatisfied(p), true);
    }

    @Test
    public void testDiscreteIsSatisfied() {

        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 10);
        List<Node> ns = Util.newNodes(mo, 10);

        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));

        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(1));
        map.addSleepingVM(vms.get(3), ns.get(1));


        Set<VM> s = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1)));
        Lonely l = new Lonely(s);

        Assert.assertEquals(l.isSatisfied(mo), true);

        s.add(vms.get(3));
        Assert.assertEquals(l.isSatisfied(mo), true);

        map.addRunningVM(vms.get(2), ns.get(0));
        Assert.assertEquals(l.isSatisfied(mo), false);

    }
}
