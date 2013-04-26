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
import btrplace.model.view.ShareableResource;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Allocate;
import btrplace.plan.event.MigrateVM;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link CumulatedResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CumulatedResourceCapacityTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2));
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
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2));
        CumulatedResourceCapacity c = new CumulatedResourceCapacity(s, "foo", 3);
        CumulatedResourceCapacity c2 = new CumulatedResourceCapacity(s, "foo", 3);
        Assert.assertTrue(c.equals(c));
        Assert.assertTrue(c.equals(c2));
        Assert.assertEquals(c.hashCode(), c2.hashCode());

        Assert.assertFalse(c.equals(new CumulatedResourceCapacity(s, "bar", 3)));
        Assert.assertFalse(c.equals(new CumulatedResourceCapacity(s, "foo", 2)));
        Assert.assertFalse(c.equals(new CumulatedResourceCapacity(new HashSet<UUID>(), "foo", 3)));
    }

    @Test
    public void testDiscreteIsSatisfied() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n3);

        Model mo = new DefaultModel(map);
        ShareableResource rc = new ShareableResource("foo", 1);
        rc.set(vm2, 2);
        mo.attach(rc);
        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2));
        CumulatedResourceCapacity cc = new CumulatedResourceCapacity(nodes, "foo", 4);
        Assert.assertEquals(cc.isSatisfied(mo), SatConstraint.Sat.SATISFIED);
        Assert.assertEquals(new CumulatedResourceCapacity(nodes, "bar", 100).isSatisfied(mo), SatConstraint.Sat.UNSATISFIED);

        rc.set(vm1, 3);
        Assert.assertEquals(cc.isSatisfied(mo), SatConstraint.Sat.UNSATISFIED);
        map.addSleepingVM(vm2, n1);
        map.addSleepingVM(vm3, n1);
        Assert.assertEquals(cc.isSatisfied(mo), SatConstraint.Sat.SATISFIED);
    }

    @Test
    public void testContinuousIsSatisfied() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n3);

        Model mo = new DefaultModel(map);
        ShareableResource rc = new ShareableResource("foo", 1);
        rc.set(vm2, 2);
        mo.attach(rc);
        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2));
        CumulatedResourceCapacity cc = new CumulatedResourceCapacity(nodes, "foo", 4);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(cc.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
        plan.add(new MigrateVM(vm4, n3, n2, 1, 2));
        Assert.assertEquals(cc.isSatisfied(plan), SatConstraint.Sat.UNSATISFIED);
        plan.add(new MigrateVM(vm1, n1, n3, 0, 1));
        Assert.assertEquals(cc.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
        plan.add(new Allocate(vm4, n2, "foo", 2, 5, 6));
        Assert.assertEquals(cc.isSatisfied(plan), SatConstraint.Sat.UNSATISFIED);
        plan.add(new Allocate(vm2, n1, "foo", 1, 4, 5));
        Assert.assertEquals(cc.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
    }
}
