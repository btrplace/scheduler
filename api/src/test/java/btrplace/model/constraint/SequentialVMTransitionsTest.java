/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link SequentialVMTransitions}.
 *
 * @author Fabien Hermenier
 */
public class SequentialVMTransitionsTest implements PremadeElements {

    @Test
    public void testInstantiation() {

        List<UUID> l = Arrays.asList(vm1, vm2, vm3);
        SequentialVMTransitions c = new SequentialVMTransitions(l);
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
        List<UUID> l = Arrays.asList(vm1, vm2, vm3);
        SequentialVMTransitions c = new SequentialVMTransitions(l);
        List<UUID> l2 = new ArrayList<>(l);
        SequentialVMTransitions c2 = new SequentialVMTransitions(l2);
        Assert.assertTrue(c.equals(c2));
        Assert.assertEquals(c.hashCode(), c2.hashCode());
        l2.add(l2.remove(0)); //shift the list
        Assert.assertFalse(c.equals(c2));
    }

    @Test
    public void testContinuousIsSatisfied() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        map.addReadyVM(vm2);
        map.addSleepingVM(vm3, n1);
        map.addRunningVM(vm4, n1);
        List<UUID> l = Arrays.asList(vm1, vm2, vm3, vm4);
        SequentialVMTransitions c = new SequentialVMTransitions(l);
        Model mo = new DefaultModel(map);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        plan.add(new MigrateVM(vm4, n1, n2, 0, 1));
        plan.add(new SuspendVM(vm1, n1, n1, 2, 3));
        plan.add(new BootVM(vm2, n1, 3, 4));
        plan.add(new ResumeVM(vm3, n1, n1, 4, 5));
        Assert.assertEquals(c.isSatisfied(plan), true);

        //Overlap
        plan = new DefaultReconfigurationPlan(mo);
        plan.add(new BootVM(vm2, n1, 3, 4));
        plan.add(new ResumeVM(vm3, n1, n1, 3, 5));
        Assert.assertEquals(c.isSatisfied(plan), false);

        //Not the right precedence
        plan = new DefaultReconfigurationPlan(mo);
        plan.add(new BootVM(vm2, n1, 3, 4));
        plan.add(new ResumeVM(vm3, n1, n1, 0, 1));
        Assert.assertEquals(c.isSatisfied(plan), false);
    }

    @Test
    public void testContinuousSatisfied2() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addReadyVM(vm1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n1);

        Model mo = new DefaultModel(map);
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);

        p.add(new BootVM(vm1, n1, 0, 1));
        p.add(new SuspendVM(vm3, n2, n2, 1, 2));
        p.add(new ShutdownVM(vm4, n1, 2, 3));

        List<UUID> seq = Arrays.asList(vm1, vm2, vm3, vm4);

        SequentialVMTransitions cstr = new SequentialVMTransitions(seq);
        Assert.assertEquals(cstr.isSatisfied(p), true);
    }
}
