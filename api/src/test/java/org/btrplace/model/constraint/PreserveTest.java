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
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.AllocateEvent;
import org.btrplace.plan.event.MigrateVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link Preserve}.
 *
 * @author Fabien Hermenier
 */
public class PreserveTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Preserve p = new Preserve(v, "cpu", 3);
        Assert.assertNotNull(p.getChecker());
        Assert.assertTrue(p.getInvolvedVMs().contains(v));
        Assert.assertTrue(p.getInvolvedNodes().isEmpty());
        Assert.assertEquals(3, p.getAmount());
        Assert.assertEquals("cpu", p.getResource());
        Assert.assertFalse(p.toString().contains("null"));
        Assert.assertFalse(p.isContinuous());
        Assert.assertFalse(p.setContinuous(true));
        System.out.println(p);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsAndHashCode() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Preserve p = new Preserve(v, "cpu", 3);
        Preserve p2 = new Preserve(v, "cpu", 3);
        Assert.assertTrue(p.equals(p));
        Assert.assertTrue(p2.equals(p));
        Assert.assertEquals(p2.hashCode(), p.hashCode());
        Assert.assertFalse(new Preserve(v, "mem", 3).equals(p));
        Assert.assertFalse(new Preserve(v, "cpu", 2).equals(p));
        Assert.assertFalse(new Preserve(mo.newVM(), "cpu", 3).equals(p));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testIsSatisfied() {
        Model m = new DefaultModel();
        List<VM> vms = Util.newVMs(m, 5);
        List<Node> ns = Util.newNodes(m, 5);

        ShareableResource rc = new ShareableResource("cpu", 3, 3);
        Mapping map = m.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addSleepingVM(vms.get(1), ns.get(0));
        map.addRunningVM(vms.get(2), ns.get(0));
        m.attach(rc);
        Set<VM> s = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1), vms.get(2)));
        Preserve p = new Preserve(vms.get(0), "cpu", 3);
        rc.setConsumption(vms.get(0), 3);
        rc.setConsumption(vms.get(1), 1); //Not running so we don't care
        rc.setConsumption(vms.get(2), 3);
        Assert.assertEquals(true, p.isSatisfied(m));

        rc.unset(vms.get(2)); //Set to 3 by default
        Assert.assertEquals(true, p.isSatisfied(m));
        Assert.assertEquals(false, new Preserve(vms.get(2), "mem", 3).isSatisfied(m));

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(m);
        rc.setConsumption(vms.get(1), 1);
        Assert.assertFalse(new Preserve(vms.get(2), "cpu", 4).isSatisfied(plan));
        plan.add(new Allocate(vms.get(2), ns.get(0), "cpu", 7, 5, 7));
        Assert.assertTrue(p.isSatisfied(plan));
        rc.setConsumption(vms.get(0), 1);
        AllocateEvent e = new AllocateEvent(vms.get(0), "cpu", 4);
        Assert.assertFalse(p.isSatisfied(plan));
        MigrateVM mig = new MigrateVM(vms.get(0), ns.get(0), ns.get(1), 0, 3);
        mig.addEvent(Action.Hook.POST, e);
        plan.add(mig);
        Assert.assertTrue(p.isSatisfied(plan));
    }
}
