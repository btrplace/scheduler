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

import btrplace.model.*;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ResumeVM;
import btrplace.plan.event.SuspendVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link SequentialVMTransitions}.
 *
 * @author Fabien Hermenier
 */
public class SequentialVMTransitionsTest {

    @Test
    public void testInstantiation() {

        List<UUID> l = new ArrayList<UUID>();
        l.add(UUID.randomUUID());
        l.add(UUID.randomUUID());
        l.add(UUID.randomUUID());
        SequentialVMTransitions c = new SequentialVMTransitions(l);
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
        List<UUID> l = new ArrayList<UUID>();
        l.add(UUID.randomUUID());
        l.add(UUID.randomUUID());
        l.add(UUID.randomUUID());
        SequentialVMTransitions c = new SequentialVMTransitions(l);
        List<UUID> l2 = new ArrayList<UUID>(l);
        SequentialVMTransitions c2 = new SequentialVMTransitions(l2);
        Assert.assertTrue(c.equals(c2));
        Assert.assertEquals(c.hashCode(), c2.hashCode());
        l2.add(l2.remove(0)); //shift the list
        Assert.assertFalse(c.equals(c2));
    }

    @Test
    public void testContinuousIsSatisfied() {
        Mapping map = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        UUID vm4 = UUID.randomUUID();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        map.addReadyVM(vm2);
        map.addSleepingVM(vm3, n1);
        map.addRunningVM(vm4, n1);
        List<UUID> l = new ArrayList<UUID>();
        l.add(vm1);
        l.add(vm2);
        l.add(vm3);
        l.add(vm4);
        SequentialVMTransitions c = new SequentialVMTransitions(l);
        Model mo = new DefaultModel(map);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        plan.add(new MigrateVM(vm4, n1, n2, 0, 1));
        plan.add(new SuspendVM(vm1, n1, n1, 2, 3));
        plan.add(new BootVM(vm2, n1, 3, 4));
        plan.add(new ResumeVM(vm3, n1, n1, 4, 5));
        Assert.assertEquals(c.isSatisfied(plan), SatConstraint.Sat.SATISFIED);

        //Overlap
        plan = new DefaultReconfigurationPlan(mo);
        plan.add(new BootVM(vm2, n1, 3, 4));
        plan.add(new ResumeVM(vm3, n1, n1, 3, 5));
        Assert.assertEquals(c.isSatisfied(plan), SatConstraint.Sat.UNSATISFIED);

        //Not the right precedence
        plan = new DefaultReconfigurationPlan(mo);
        plan.add(new BootVM(vm2, n1, 3, 4));
        plan.add(new ResumeVM(vm3, n1, n1, 0, 1));
        Assert.assertEquals(c.isSatisfied(plan), SatConstraint.Sat.UNSATISFIED);

    }
}
