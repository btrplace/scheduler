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
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.MigrateVM;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link Gather}.
 *
 * @author Fabien Hermenier
 */
public class GatherTest implements PremadeElements {

    @Test
    public void testInstantiate() {
        Set<Integer> s = new HashSet<>(Arrays.asList(vm1, vm2));
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
        Set<Integer> s = new HashSet<>(Arrays.asList(vm1, vm2));
        Gather g = new Gather(s);
        Assert.assertTrue(g.equals(g));
        Assert.assertFalse(g.equals(new Object()));
        Gather g2 = new Gather(new HashSet<>(s));
        Assert.assertTrue(g2.equals(g));
        Assert.assertEquals(g2.hashCode(), g.hashCode());
        s.remove(vm1);
        Assert.assertFalse(g2.equals(g));
    }

    @Test
    public void testDiscreteIsSatisfied() {
        Set<Integer> s = new HashSet<>(Arrays.asList(vm1, vm2));
        Gather g = new Gather(s);

        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();


        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        map.addReadyVM(vm2);

        Assert.assertEquals(g.isSatisfied(mo), true);
        map.addRunningVM(vm2, n1);
        Assert.assertEquals(g.isSatisfied(mo), true);
        map.addRunningVM(vm2, n2);
        Assert.assertEquals(g.isSatisfied(mo), false);
    }

    @Test(dependsOnMethods = {"testDiscreteIsSatisfied"})
    public void testContinuousIsSatisfied() {
        Set<Integer> s = new HashSet<>(Arrays.asList(vm1, vm2));
        Gather g = new Gather(s);
        g.setContinuous(true);
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        map.addReadyVM(vm2);
        map.addRunningVM(vm2, n2);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(g.isSatisfied(plan), false);

        map.addReadyVM(vm2);
        Assert.assertEquals(g.isSatisfied(plan), true);
        plan.add(new BootVM(vm2, n1, 0, 1));
        Assert.assertEquals(g.isSatisfied(plan), true);

        map.addRunningVM(vm2, n1);
        plan = new DefaultReconfigurationPlan(mo);
        plan.add(new MigrateVM(vm2, n1, n2, 0, 1));
        plan.add(new MigrateVM(vm1, n1, n2, 0, 1));
        Assert.assertEquals(g.isSatisfied(plan), false);
    }
}
