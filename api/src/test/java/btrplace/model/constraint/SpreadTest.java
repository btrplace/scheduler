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
 * Unit tests for {@link btrplace.model.constraint.Spread}.
 *
 * @author Fabien Hermenier
 */
public class SpreadTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<Integer> x = new HashSet<>(Arrays.asList(vm1, vm2));
        Spread s = new Spread(x);
        Assert.assertNotNull(s.getChecker());
        Assert.assertEquals(x, s.getInvolvedVMs());
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertTrue(s.isContinuous());
        Assert.assertNotNull(s.toString());
        Assert.assertTrue(s.setContinuous(false));
        Assert.assertFalse(s.isContinuous());
        System.out.println(s);

        s = new Spread(x, false);
        Assert.assertFalse(s.isContinuous());
    }

    @Test
    public void testEquals() {
        Set<Integer> x = new HashSet<>(Arrays.asList(vm1, vm2));
        Spread s = new Spread(x);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Spread(x).equals(s));
        Assert.assertEquals(s.hashCode(), new Spread(x).hashCode());
        x = new HashSet<>(Arrays.asList(vm3));
        Assert.assertFalse(new Spread(x).equals(s));
    }

    /**
     * test isSatisfied() in the discrete mode.
     */
    @Test
    public void testDiscreteIsSatisfied() {

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOnlineNode(n4);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n1);

        Model mo = new DefaultModel(map);

        //Discrete satisfaction.
        Spread s = new Spread(map.getAllVMs());
        s.setContinuous(false);

        Assert.assertEquals(s.isSatisfied(mo), false);
        map.addRunningVM(vm1, n4);
        Assert.assertEquals(s.isSatisfied(mo), true);
    }

    @Test
    public void testContinuousIsSatisfied() {

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOnlineNode(n4);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);

        Spread s = new Spread(map.getAllVMs());

        Model mo = new DefaultModel(map);
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(s.isSatisfied(p), true);

        MigrateVM m1 = new MigrateVM(vm1, n1, n2, 1, 2);
        p.add(m1);
        Assert.assertEquals(s.isSatisfied(p), false);

        //No overlapping at moment 1
        MigrateVM m2 = new MigrateVM(vm2, n2, n3, 0, 1);
        p.add(m2);
        Assert.assertEquals(s.isSatisfied(p), true);


        map.addRunningVM(vm3, n2);
        s = new Spread(map.getAllVMs());
        p = new DefaultReconfigurationPlan(mo);
        System.out.println(p.getOrigin() + "\n" + p.getResult());
        Assert.assertEquals(s.isSatisfied(p), false);
        p.add(new MigrateVM(vm3, n2, n3, 0, 5));
        Assert.assertEquals(s.isSatisfied(p), true);
    }
}
