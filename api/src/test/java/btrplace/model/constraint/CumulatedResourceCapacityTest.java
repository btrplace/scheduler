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

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.view.ShareableResource;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link CumulatedResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CumulatedResourceCapacityTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<Integer> s = new HashSet<>(Arrays.asList(n1, n2));
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
        Set<Integer> s = new HashSet<>(Arrays.asList(n1, n2));
        CumulatedResourceCapacity c = new CumulatedResourceCapacity(s, "foo", 3);
        CumulatedResourceCapacity c2 = new CumulatedResourceCapacity(s, "foo", 3);
        Assert.assertTrue(c.equals(c));
        Assert.assertTrue(c.equals(c2));
        Assert.assertEquals(c.hashCode(), c2.hashCode());

        Assert.assertFalse(c.equals(new CumulatedResourceCapacity(s, "bar", 3)));
        Assert.assertFalse(c.equals(new CumulatedResourceCapacity(s, "foo", 2)));
        Assert.assertFalse(c.equals(new CumulatedResourceCapacity(new HashSet<Integer>(), "foo", 3)));
    }

    @Test
    public void testDiscreteIsSatisfied() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n3);

        ShareableResource rc = new ShareableResource("foo", 1);
        rc.set(vm2, 2);
        mo.attach(rc);
        Set<Integer> nodes = new HashSet<>(Arrays.asList(n1, n2));
        CumulatedResourceCapacity cc = new CumulatedResourceCapacity(nodes, "foo", 4);
        Assert.assertEquals(cc.isSatisfied(mo), true);
        Assert.assertEquals(new CumulatedResourceCapacity(nodes, "bar", 100).isSatisfied(mo), false);

        rc.set(vm1, 3);
        Assert.assertEquals(cc.isSatisfied(mo), false);
        map.addSleepingVM(vm2, n1);
        map.addSleepingVM(vm3, n1);
        Assert.assertEquals(cc.isSatisfied(mo), true);
    }

    @Test
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n3);
        map.addReadyVM(vm5);
        ShareableResource rc = new ShareableResource("foo", 1);
        mo.attach(rc);

        Set<Integer> nodes = new HashSet<>(Arrays.asList(n1, n2));
        CumulatedResourceCapacity cc = new CumulatedResourceCapacity(nodes, "foo", 4, true);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(cc.isSatisfied(plan), true);
        //3/4
        MigrateVM m = new MigrateVM(vm4, n3, n2, 0, 1);
        m.addEvent(Action.Hook.post, new AllocateEvent(vm4, "foo", 2));
        plan.add(m);
        //5/4
        plan.add(new ShutdownVM(vm3, n2, 1, 2));
        //4/4
        plan.add(new BootVM(vm5, n3, 2, 3));
        //4/4
        plan.add(new Allocate(vm2, n1, "foo", 2, 2, 3));
        //5/4
        plan.add(new MigrateVM(vm1, n1, n3, 3, 4));
        System.out.println(plan);
        Assert.assertEquals(cc.isSatisfied(plan), true);

    }
}
