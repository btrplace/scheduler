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
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownVM;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link Lonely}.
 *
 * @author Fabien Hermenier
 */
public class LonelyTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));
        Lonely l = new Lonely(s);
        Assert.assertNotNull(l.getChecker());
        Assert.assertFalse(l.toString().contains("null"));
        Assert.assertEquals(l.getInvolvedVMs(), s);
        Assert.assertTrue(l.getInvolvedNodes().isEmpty());
        Assert.assertFalse(l.isContinuous());
        Assert.assertTrue(l.setContinuous(true));
        Assert.assertTrue(l.setContinuous(false));
        System.out.println(l);

        l = new Lonely(s, true);
        Assert.assertTrue(l.isContinuous());
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsHashCode() {
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));
        Lonely l = new Lonely(s);
        Assert.assertTrue(l.equals(l));
        Assert.assertTrue(l.equals(new Lonely(new HashSet<UUID>(s))));
        Assert.assertEquals(l.hashCode(), new Lonely(new HashSet<UUID>(s)).hashCode());
        Assert.assertFalse(l.equals(new Lonely(new HashSet<UUID>())));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testContinuousIsSatisfied() {
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(vm1, vm2));

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n2);

        Model mo = new DefaultModel(map);

        Lonely l = new Lonely(s);

        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(l.isSatisfied(p), SatConstraint.Sat.SATISFIED);
        p.add(new MigrateVM(vm2, n1, n2, 2, 4));

        Assert.assertEquals(l.isSatisfied(p), SatConstraint.Sat.UNSATISFIED);
        p.add(new ShutdownVM(vm3, n2, 0, 1));
        Assert.assertEquals(l.isSatisfied(p), SatConstraint.Sat.UNSATISFIED);
        p.add(new MigrateVM(vm4, n2, n3, 1, 2));
        Assert.assertEquals(l.isSatisfied(p), SatConstraint.Sat.SATISFIED);
    }

    @Test
    public void testDiscreteIsSatisfied() {


        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addSleepingVM(vm4, n2);

        Model mo = new DefaultModel(map);

        Set<UUID> s = new HashSet<UUID>(Arrays.asList(vm1, vm2));
        Lonely l = new Lonely(s);

        Assert.assertEquals(l.isSatisfied(mo), SatConstraint.Sat.SATISFIED);

        s.add(vm4);
        Assert.assertEquals(l.isSatisfied(mo), SatConstraint.Sat.SATISFIED);

        map.addRunningVM(vm3, n1);
        Assert.assertEquals(l.isSatisfied(mo), SatConstraint.Sat.UNSATISFIED);

    }
}
