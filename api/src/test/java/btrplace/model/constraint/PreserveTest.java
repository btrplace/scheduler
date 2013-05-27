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

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.view.ShareableResource;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.plan.event.Allocate;
import btrplace.plan.event.AllocateEvent;
import btrplace.plan.event.MigrateVM;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

;

/**
 * Unit tests for {@link Preserve}.
 *
 * @author Fabien Hermenier
 */
public class PreserveTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<Integer> vms = new HashSet<>(Arrays.asList(vm1, vm2));
        Preserve p = new Preserve(vms, "cpu", 3);
        Assert.assertNotNull(p.getChecker());
        Assert.assertEquals(vms, p.getInvolvedVMs());
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
        Set<Integer> vms = new HashSet<>(Arrays.asList(vm1, vm2));
        Preserve p = new Preserve(vms, "cpu", 3);
        Preserve p2 = new Preserve(vms, "cpu", 3);
        Assert.assertTrue(p.equals(p));
        Assert.assertTrue(p2.equals(p));
        Assert.assertEquals(p2.hashCode(), p.hashCode());
        Assert.assertFalse(new Preserve(vms, "mem", 3).equals(p));
        Assert.assertFalse(new Preserve(vms, "cpu", 2).equals(p));
        Assert.assertFalse(new Preserve(new HashSet<Integer>(), "cpu", 3).equals(p));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testIsSatisfied() {
        ShareableResource rc = new ShareableResource("cpu", 3);
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        map.addSleepingVM(vm2, n1);
        map.addRunningVM(vm3, n1);
        Model m = new DefaultModel(map);
        m.attach(rc);
        Set<Integer> s = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        Preserve p = new Preserve(s, "cpu", 3);
        rc.set(vm1, 3);
        rc.set(vm2, 1); //Not running so we don't care
        rc.set(vm3, 3);
        Assert.assertEquals(true, p.isSatisfied(m));

        rc.unset(vm3); //Set to 3 by default
        Assert.assertEquals(true, p.isSatisfied(m));
        Assert.assertEquals(false, new Preserve(s, "mem", 3).isSatisfied(m));

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(m);
        rc.set(vm3, 1);
        Assert.assertFalse(p.isSatisfied(plan));
        plan.add(new Allocate(vm3, n1, "cpu", 7, 5, 7));
        Assert.assertTrue(p.isSatisfied(plan));
        rc.set(vm1, 1);
        AllocateEvent e = new AllocateEvent(vm1, "cpu", 4);
        Assert.assertFalse(p.isSatisfied(plan));
        MigrateVM mig = new MigrateVM(vm1, n1, n2, 0, 3);
        mig.addEvent(Action.Hook.post, e);
        plan.add(mig);
        Assert.assertTrue(p.isSatisfied(plan));
    }
}
