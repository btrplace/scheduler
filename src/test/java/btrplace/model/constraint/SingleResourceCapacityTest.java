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
import btrplace.plan.event.ShutdownVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link SingleResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class SingleResourceCapacityTest {

    UUID n1 = UUID.randomUUID();
    UUID n2 = UUID.randomUUID();

    UUID vm1 = UUID.randomUUID();
    UUID vm2 = UUID.randomUUID();
    UUID vm3 = UUID.randomUUID();
    UUID vm4 = UUID.randomUUID();


    @Test
    public void testInstantiation() {
        Set<UUID> s = new HashSet<UUID>();
        s.add(UUID.randomUUID());
        s.add(UUID.randomUUID());
        SingleResourceCapacity c = new SingleResourceCapacity(s, "foo", 3);
        Assert.assertEquals(s, c.getInvolvedNodes());
        Assert.assertEquals("foo", c.getResource());
        Assert.assertEquals(3, c.getAmount());
        Assert.assertTrue(c.getInvolvedVMs().isEmpty());
        Assert.assertFalse(c.toString().contains("null"));
        System.out.println(c);
        Assert.assertFalse(c.isContinuous());
        Assert.assertTrue(c.setContinuous(true));
        Assert.assertTrue(c.isContinuous());
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsAndHashCode() {
        Set<UUID> s = new HashSet<UUID>();
        s.add(UUID.randomUUID());
        s.add(UUID.randomUUID());
        SingleResourceCapacity c = new SingleResourceCapacity(s, "foo", 3);
        SingleResourceCapacity c2 = new SingleResourceCapacity(s, "foo", 3);
        Assert.assertTrue(c.equals(c));
        Assert.assertTrue(c.equals(c2));
        Assert.assertEquals(c.hashCode(), c2.hashCode());

        Assert.assertFalse(c.equals(new SingleResourceCapacity(s, "bar", 3)));
        Assert.assertFalse(c.equals(new SingleResourceCapacity(s, "foo", 2)));
        Assert.assertFalse(c.equals(new SingleResourceCapacity(new HashSet<UUID>(), "foo", 3)));
        c.setContinuous(true);
        c2.setContinuous(false);
        Assert.assertFalse(c.equals(c2));
    }

    @Test
    public void testDiscreteIsSatisfied() {
        Mapping m = new DefaultMapping();
        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addRunningVM(vm1, n1);
        m.addReadyVM(vm2);

        m.addRunningVM(vm3, n2);
        m.addReadyVM(vm4);
        Model mo = new DefaultModel(m);

        ShareableResource rc = new DefaultShareableResource("foo", 2);
        mo.attach(rc);
        SingleResourceCapacity c = new SingleResourceCapacity(m.getAllNodes(), "foo", 3);
        Assert.assertEquals(c.isSatisfied(mo), SatConstraint.Sat.SATISFIED);
        rc.set(vm3, 4);
        Assert.assertEquals(c.isSatisfied(mo), SatConstraint.Sat.UNSATISFIED);

        rc.set(vm3, 1);
        m.addRunningVM(vm1, n2);
        Assert.assertEquals(c.isSatisfied(mo), SatConstraint.Sat.SATISFIED);
    }

    @Test
    public void testContinuousIsSatisfied() {
        Mapping m = new DefaultMapping();
        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addRunningVM(vm1, n1);
        m.addReadyVM(vm2);

        m.addRunningVM(vm3, n2);
        m.addReadyVM(vm4);
        Model mo = new DefaultModel(m);

        ShareableResource rc = new DefaultShareableResource("foo", 2);
        mo.attach(rc);
        SingleResourceCapacity c = new SingleResourceCapacity(m.getAllNodes(), "foo", 3);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(c.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
        plan.add(new BootVM(vm4, n1, 1, 2));
        Assert.assertEquals(c.isSatisfied(plan), SatConstraint.Sat.UNSATISFIED);

        plan.add(new ShutdownVM(vm1, n1, 0, 1));
        Assert.assertEquals(c.isSatisfied(plan), SatConstraint.Sat.SATISFIED);


    }

}
