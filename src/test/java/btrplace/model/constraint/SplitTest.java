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
import btrplace.plan.event.SuspendVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link Split}.
 *
 * @author Fabien Hermenier
 */
public class SplitTest extends ConstraintTestMaterial {

    @Test
    public void testInstantiation() {
        Set<UUID> s1 = Collections.singleton(vm1);
        Set<UUID> s2 = Collections.singleton(vm2);
        List<Set<UUID>> args = Arrays.asList(s1, s2);
        Split sp = new Split(args);
        Assert.assertEquals(args, sp.getSets());
        Assert.assertEquals(2, sp.getInvolvedVMs().size());
        Assert.assertTrue(sp.getInvolvedNodes().isEmpty());
        Assert.assertFalse(sp.toString().contains("null"));
        Assert.assertFalse(sp.isContinuous());
        Assert.assertTrue(sp.setContinuous(true));
        Assert.assertTrue(sp.isContinuous());
        Assert.assertTrue(sp.setContinuous(false));
        Assert.assertFalse(sp.isContinuous());
        System.out.println(sp);

        sp = new Split(args, true);
        Assert.assertTrue(sp.isContinuous());
    }

    @Test
    public void testEquals() {
        Set<UUID> s1 = Collections.singleton(vm1);
        Set<UUID> s2 = Collections.singleton(vm2);
        List<Set<UUID>> args = Arrays.asList(s1, s2);
        Split sp = new Split(args);
        Assert.assertTrue(sp.equals(sp));
        Assert.assertTrue(new Split(args).equals(sp));
        Assert.assertEquals(new Split(args).hashCode(), sp.hashCode());
        List<Set<UUID>> args2 = new ArrayList<Set<UUID>>(args);
        args2.add(Collections.singleton(UUID.randomUUID()));
        Assert.assertFalse(new Split(args2).equals(sp));
    }

    @Test
    public void testDiscreteIsSatisfied() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);


        Set<UUID> s1 = new HashSet<UUID>(Arrays.asList(vm1, vm2));
        Set<UUID> s2 = new HashSet<UUID>(Arrays.asList(vm3, vm4));
        Set<UUID> s3 = Collections.singleton(vm5);
        Set<Set<UUID>> args = new HashSet<Set<UUID>>(Arrays.asList(s1, s2, s3));

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n2);

        Split sp = new Split(args);
        Model mo = new DefaultModel(map);
        Assert.assertEquals(SatConstraint.Sat.SATISFIED, sp.isSatisfied(mo));
        map.addRunningVM(vm3, n3);
        Assert.assertEquals(SatConstraint.Sat.SATISFIED, sp.isSatisfied(mo));
        map.addRunningVM(vm3, n1);
        Assert.assertEquals(SatConstraint.Sat.UNSATISFIED, sp.isSatisfied(mo));
    }

    @Test
    public void testContinuousIsSatisfied() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        Set<UUID> s1 = new HashSet<UUID>(Arrays.asList(vm1, vm2));
        Set<UUID> s2 = new HashSet<UUID>(Arrays.asList(vm3, vm4));
        Set<UUID> s3 = Collections.singleton(vm5);
        Set<Set<UUID>> args = new HashSet<Set<UUID>>(Arrays.asList(s1, s2, s3));

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n2);

        Split sp = new Split(args);
        Model mo = new DefaultModel(map);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(sp.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
        map.addRunningVM(vm3, n1); //Violation
        Assert.assertEquals(sp.isSatisfied(plan), SatConstraint.Sat.UNSATISFIED);

        plan.add(new MigrateVM(vm3, n1, n2, 0, 1));
        Assert.assertEquals(sp.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
        //Temporary overlap
        plan.add(new MigrateVM(vm3, n2, n1, 5, 6));
        Assert.assertEquals(sp.isSatisfied(plan), SatConstraint.Sat.UNSATISFIED);
        //Liberate n1 from vm1 and vm2 before
        plan.add(new SuspendVM(vm1, n1, n1, 2, 3));
        plan.add(new ShutdownVM(vm2, n1, 2, 3));
        Assert.assertEquals(sp.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
    }
}
