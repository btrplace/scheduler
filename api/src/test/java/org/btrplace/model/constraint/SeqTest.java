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
import org.btrplace.plan.event.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link Seq}.
 *
 * @author Fabien Hermenier
 */
public class SeqTest {

    @Test
    public void testInstantiation() {

        Model mo = new DefaultModel();
        List<VM> l = Arrays.asList(mo.newVM(), mo.newVM(), mo.newVM());
        Seq c = new Seq(l);
        Assert.assertNotNull(c.getChecker());
        Assert.assertEquals(l, c.getInvolvedVMs());
        Assert.assertTrue(c.getInvolvedNodes().isEmpty());
        Assert.assertFalse(c.toString().contains("null"));
        Assert.assertTrue(c.isContinuous());
        Assert.assertFalse(c.setContinuous(false));
        Assert.assertTrue(c.setContinuous(true));
        System.out.println(c);

    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEquals() {
        Model mo = new DefaultModel();
        List<VM> l = Arrays.asList(mo.newVM(), mo.newVM(), mo.newVM());
        Seq c = new Seq(l);
        List<VM> l2 = new ArrayList<>(l);
        Seq c2 = new Seq(l2);
        Assert.assertTrue(c.equals(c2));
        Assert.assertEquals(c.hashCode(), c2.hashCode());
        l2.add(l2.remove(0)); //shift the list
        Assert.assertFalse(c.equals(c2));
    }

    @Test
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 5);
        List<VM> vms = Util.newVMs(mo, 5);
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addReadyVM(vms.get(1));
        map.addSleepingVM(vms.get(2), ns.get(0));
        map.addRunningVM(vms.get(3), ns.get(0));
        List<VM> l = Arrays.asList(vms.get(0), vms.get(1), vms.get(2), vms.get(3));
        Seq c = new Seq(l);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        plan.add(new MigrateVM(vms.get(3), ns.get(0), ns.get(1), 0, 1));
        plan.add(new SuspendVM(vms.get(0), ns.get(0), ns.get(0), 2, 3));
        plan.add(new BootVM(vms.get(1), ns.get(0), 3, 4));
        plan.add(new ResumeVM(vms.get(2), ns.get(0), ns.get(0), 4, 5));
        Assert.assertEquals(c.isSatisfied(plan), true);

        //Overlap
        plan = new DefaultReconfigurationPlan(mo);
        plan.add(new BootVM(vms.get(1), ns.get(0), 3, 4));
        plan.add(new ResumeVM(vms.get(2), ns.get(0), ns.get(0), 3, 5));
        Assert.assertEquals(c.isSatisfied(plan), false);

        //Not the right precedence
        plan = new DefaultReconfigurationPlan(mo);
        plan.add(new BootVM(vms.get(1), ns.get(0), 3, 4));
        plan.add(new ResumeVM(vms.get(2), ns.get(0), ns.get(0), 0, 1));
        Assert.assertEquals(c.isSatisfied(plan), false);
    }

    @Test
    public void testContinuousSatisfied2() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 5);
        List<VM> vms = Util.newVMs(mo, 5);
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addReadyVM(vms.get(0));
        map.addRunningVM(vms.get(1), ns.get(0));
        map.addRunningVM(vms.get(2), ns.get(1));
        map.addRunningVM(vms.get(3), ns.get(0));
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);

        p.add(new BootVM(vms.get(0), ns.get(0), 0, 1));
        p.add(new SuspendVM(vms.get(2), ns.get(1), ns.get(1), 1, 2));
        p.add(new ShutdownVM(vms.get(3), ns.get(0), 2, 3));

        List<VM> seq = Arrays.asList(vms.get(0), vms.get(1), vms.get(2), vms.get(3));

        Seq cstr = new Seq(seq);
        Assert.assertEquals(cstr.isSatisfied(p), true);
    }
}
