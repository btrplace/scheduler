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
import btrplace.plan.event.ResumeVM;
import btrplace.plan.event.ShutdownVM;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link SingleRunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class SingleRunningCapacityTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<Integer> s = new HashSet<>(Arrays.asList(n1, n2));

        SingleRunningCapacity c = new SingleRunningCapacity(s, 3);
        Assert.assertNotNull(c.getChecker());
        Assert.assertEquals(s, c.getInvolvedNodes());
        Assert.assertEquals(3, c.getAmount());
        Assert.assertTrue(c.getInvolvedVMs().isEmpty());
        Assert.assertFalse(c.toString().contains("null"));
        Assert.assertFalse(c.isContinuous());
        Assert.assertTrue(c.setContinuous(true));
        Assert.assertTrue(c.isContinuous());
        System.out.println(c);

        c = new SingleRunningCapacity(s, 3, true);
        Assert.assertTrue(c.isContinuous());

    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsAndHashCode() {
        Set<Integer> s = new HashSet<>(Arrays.asList(n1, n2));
        SingleRunningCapacity c = new SingleRunningCapacity(s, 3);
        SingleRunningCapacity c2 = new SingleRunningCapacity(s, 3);
        Assert.assertTrue(c.equals(c));
        Assert.assertTrue(c.equals(c2));
        Assert.assertEquals(c.hashCode(), c2.hashCode());

        Assert.assertFalse(c.equals(new SingleRunningCapacity(s, 2)));
        Assert.assertFalse(c.equals(new SingleRunningCapacity(new HashSet<Integer>(), 3)));
    }

    @Test
    public void testDiscreteIsSatisfied() {
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addRunningVM(vm1, n1);
        m.addReadyVM(vm2);

        m.addRunningVM(vm3, n2);
        m.addReadyVM(vm4);

        SingleRunningCapacity c = new SingleRunningCapacity(m.getAllNodes(), 1);

        Assert.assertEquals(c.isSatisfied(mo), true);
        m.addRunningVM(vm2, n2);
        Assert.assertEquals(c.isSatisfied(mo), false);
    }


    @Test
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addRunningVM(vm1, n1);
        m.addReadyVM(vm2);

        m.addRunningVM(vm3, n2);
        m.addReadyVM(vm4);

        SingleRunningCapacity c = new SingleRunningCapacity(m.getAllNodes(), 1);
        c.setContinuous(true);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(c.isSatisfied(plan), true);

        //Bad resulting configuration
        plan.add(new BootVM(vm2, n1, 1, 2));
        Assert.assertEquals(c.isSatisfied(plan), false);

        //bad initial configuration
        m.addRunningVM(vm2, n1);
        plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(c.isSatisfied(plan), false);


        //Already satisfied && continuous satisfaction
        m.addSleepingVM(vm2, n1);
        plan = new DefaultReconfigurationPlan(mo);
        plan.add(new ShutdownVM(vm1, n1, 0, 1));
        plan.add(new ResumeVM(vm2, n1, n1, 1, 2));
        Assert.assertEquals(c.isSatisfied(plan), true);
    }
}
