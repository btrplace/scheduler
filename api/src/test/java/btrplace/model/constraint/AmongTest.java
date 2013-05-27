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
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.MigrateVM;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

;

/**
 * Unit tests for {@link Among}.
 *
 * @author Fabien Hermenier
 */
public class AmongTest implements PremadeElements {

    @Test
    public void testInstantiation() {

        Set<Integer> s1 = new HashSet<>(Arrays.asList(n1, n2));
        Set<Integer> s2 = new HashSet<>(Arrays.asList(n3));
        Set<Set<Integer>> pGrps = new HashSet<>(Arrays.asList(s1, s2));
        Set<Integer> vms = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        Among a = new Among(vms, pGrps);
        Assert.assertNotNull(a.getChecker());
        Assert.assertEquals(a.getInvolvedVMs(), vms);
        Assert.assertEquals(a.getGroupsOfNodes(), pGrps);
        Assert.assertEquals(a.getInvolvedNodes().size(), s1.size() + s2.size());
        Assert.assertTrue(a.getInvolvedNodes().containsAll(s1));
        Assert.assertTrue(a.getInvolvedNodes().containsAll(s2));
        Assert.assertFalse(a.toString().contains("null"));
        Assert.assertFalse(a.isContinuous());
        Assert.assertTrue(a.setContinuous(true));
        Assert.assertTrue(a.setContinuous(false));

        a = new Among(vms, pGrps, true);
        Assert.assertTrue(a.isContinuous());
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsHashCode() {

        Set<Integer> s1 = new HashSet<>(Arrays.asList(n1, n2));
        Set<Integer> s2 = new HashSet<>(Arrays.asList(n3));
        Set<Set<Integer>> pGrps = new HashSet<>(Arrays.asList(s1, s2));
        Set<Integer> vms = new HashSet<>(Arrays.asList(vm1, vm2, vm3));

        Among a = new Among(vms, pGrps);
        Assert.assertTrue(a.equals(a));
        Assert.assertTrue(a.equals(new Among(new HashSet<>(vms), pGrps)));
        Assert.assertEquals(a.hashCode(), new Among(new HashSet<>(vms), pGrps).hashCode());
        Assert.assertFalse(a.equals(new Among(new HashSet<Integer>(), pGrps)));
        Assert.assertFalse(a.equals(new Among(new HashSet<>(vms), new HashSet<Set<Integer>>())));
        Among a2 = new Among(new HashSet<>(vms), new HashSet<Set<Integer>>());
        a2.setContinuous(true);
        Assert.assertFalse(a.equals(a2));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testDiscreteIsSatisfied() {

        Set<Integer> s1 = new HashSet<>(Arrays.asList(n1, n2));
        Set<Integer> s2 = new HashSet<>(Arrays.asList(n3));
        Set<Set<Integer>> pGrps = new HashSet<>(Arrays.asList(s1, s2));
        Set<Integer> vms = new HashSet<>(Arrays.asList(vm1, vm2, vm3));

        Among a = new Among(vms, pGrps);

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addSleepingVM(vm3, n3);

        Model mo = new DefaultModel(map);
        Assert.assertEquals(a.isSatisfied(mo), true);
        map.addRunningVM(vm3, n3);
        Assert.assertEquals(a.isSatisfied(mo), false);
        map.addSleepingVM(vm3, n2);
        Assert.assertEquals(a.isSatisfied(mo), true);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testContinuousIsSatisfied() {

        Set<Integer> s1 = new HashSet<>(Arrays.asList(n1, n2));
        Set<Integer> s2 = new HashSet<>(Arrays.asList(n3));
        Set<Set<Integer>> pGrps = new HashSet<>(Arrays.asList(s1, s2));
        Set<Integer> vms = new HashSet<>(Arrays.asList(vm1, vm2, vm3));

        Among a = new Among(vms, pGrps, true);

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n2);
        Model mo = new DefaultModel(map);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(a.isSatisfied(plan), true);

        plan.add(new MigrateVM(vm3, n2, n3, 0, 1));
        plan.add(new MigrateVM(vm3, n3, n2, 1, 2));
        //At moment 1, the constraint will be violated
        Assert.assertEquals(a.isSatisfied(plan), false);
    }
}
