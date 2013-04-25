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
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link Among}.
 *
 * @author Fabien Hermenier
 */
public class AmongTest implements PremadeElements {

    @Test
    public void testInstantiation() {

        Set<UUID> s1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> s2 = new HashSet<UUID>(Arrays.asList(n3));
        Set<Set<UUID>> pGrps = new HashSet<Set<UUID>>(Arrays.asList(s1, s2));
        Set<UUID> vms = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));
        Among a = new Among(vms, pGrps);
        Assert.assertNotNull(a.getValidator());
        Assert.assertEquals(a.getInvolvedVMs(), vms);
        Assert.assertEquals(a.getGroupsOfNodes(), pGrps);
        Assert.assertEquals(a.getInvolvedNodes().size(), s1.size() + s2.size());
        Assert.assertTrue(a.getInvolvedNodes().containsAll(s1));
        Assert.assertTrue(a.getInvolvedNodes().containsAll(s2));
        System.out.println(a);
        Assert.assertFalse(a.toString().contains("null"));
        Assert.assertFalse(a.isContinuous());
        Assert.assertTrue(a.setContinuous(true));
        Assert.assertTrue(a.setContinuous(false));

        a = new Among(vms, pGrps, true);
        Assert.assertTrue(a.isContinuous());
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsHashCode() {

        Set<UUID> s1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> s2 = new HashSet<UUID>(Arrays.asList(n3));
        Set<Set<UUID>> pGrps = new HashSet<Set<UUID>>(Arrays.asList(s1, s2));
        Set<UUID> vms = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));

        Among a = new Among(vms, pGrps);
        Assert.assertTrue(a.equals(a));
        Assert.assertTrue(a.equals(new Among(new HashSet<UUID>(vms), pGrps)));
        Assert.assertEquals(a.hashCode(), new Among(new HashSet<UUID>(vms), pGrps).hashCode());
        Assert.assertFalse(a.equals(new Among(new HashSet<UUID>(), pGrps)));
        Assert.assertFalse(a.equals(new Among(new HashSet<UUID>(vms), new HashSet<Set<UUID>>())));
        Among a2 = new Among(new HashSet<UUID>(vms), new HashSet<Set<UUID>>());
        a2.setContinuous(true);
        Assert.assertFalse(a.equals(a2));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testDiscreteIsSatisfied() {

        Set<UUID> s1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> s2 = new HashSet<UUID>(Arrays.asList(n3));
        Set<Set<UUID>> pGrps = new HashSet<Set<UUID>>(Arrays.asList(s1, s2));
        Set<UUID> vms = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));

        Among a = new Among(vms, pGrps);

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addSleepingVM(vm3, n3);

        Model mo = new DefaultModel(map);
        Assert.assertEquals(a.isSatisfied(mo), SatConstraint.Sat.SATISFIED);
        map.addRunningVM(vm3, n3);
        Assert.assertEquals(a.isSatisfied(mo), SatConstraint.Sat.UNSATISFIED);
        map.addSleepingVM(vm3, n2);
        Assert.assertEquals(a.isSatisfied(mo), SatConstraint.Sat.SATISFIED);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testContinuousIsSatisfied() {

        Set<UUID> s1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> s2 = new HashSet<UUID>(Arrays.asList(n3));
        Set<Set<UUID>> pGrps = new HashSet<Set<UUID>>(Arrays.asList(s1, s2));
        Set<UUID> vms = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));

        Among a = new Among(vms, pGrps);

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n2);
        Model mo = new DefaultModel(map);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(a.isSatisfied(plan), SatConstraint.Sat.SATISFIED);

        plan.add(new MigrateVM(vm3, n2, n3, 0, 1));
        plan.add(new MigrateVM(vm3, n3, n2, 1, 2));
        //At moment 1, the constraint will be violated
        Assert.assertEquals(a.isSatisfied(plan), SatConstraint.Sat.UNSATISFIED);
    }
}
