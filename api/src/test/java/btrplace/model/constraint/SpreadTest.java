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

import btrplace.model.*;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.MigrateVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link btrplace.model.constraint.Spread}.
 *
 * @author Fabien Hermenier
 */
public class SpreadTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        Set<VM> x = new HashSet<>(Arrays.asList(mo.newVM(), mo.newVM()));
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
        Model mo = new DefaultModel();
        Set<VM> x = new HashSet<>(Arrays.asList(mo.newVM(), mo.newVM()));
        Spread s = new Spread(x);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Spread(x).equals(s));
        Assert.assertEquals(s.hashCode(), new Spread(x).hashCode());
        Assert.assertNotEquals(s.hashCode(), new Spread(new HashSet<VM>()).hashCode());
        x = new HashSet<>(Arrays.asList(mo.newVM()));
        Assert.assertFalse(new Spread(x).equals(s));
    }

    /**
     * test isSatisfied() in the discrete mode.
     */
    @Test
    public void testDiscreteIsSatisfied() {

        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 4);
        List<VM> vms = Util.newVMs(mo, 3);
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));
        map.addOnlineNode(ns.get(3));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(1));
        map.addRunningVM(vms.get(2), ns.get(0));


        //Discrete satisfaction.
        Spread s = new Spread(map.getAllVMs());
        s.setContinuous(false);

        Assert.assertEquals(s.isSatisfied(mo), false);
        map.addRunningVM(vms.get(0), ns.get(3));
        Assert.assertEquals(s.isSatisfied(mo), true);
    }

    @Test
    public void testContinuousIsSatisfied() {

        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 4);
        List<VM> vms = Util.newVMs(mo, 4);

        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));
        map.addOnlineNode(ns.get(3));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(1));

        Spread s = new Spread(map.getAllVMs());

        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(s.isSatisfied(p), true);

        MigrateVM m1 = new MigrateVM(vms.get(0), ns.get(0), ns.get(1), 1, 2);
        p.add(m1);
        Assert.assertEquals(s.isSatisfied(p), false);

        //No overlapping at moment 1
        MigrateVM m2 = new MigrateVM(vms.get(1), ns.get(1), ns.get(2), 0, 1);
        p.add(m2);
        Assert.assertEquals(s.isSatisfied(p), true);


        map.addRunningVM(vms.get(2), ns.get(1));
        s = new Spread(map.getAllVMs());
        p = new DefaultReconfigurationPlan(mo);
        System.out.println(p.getOrigin() + "\n" + p.getResult());
        Assert.assertEquals(s.isSatisfied(p), false);
        p.add(new MigrateVM(vms.get(2), ns.get(1), ns.get(2), 0, 5));
        Assert.assertEquals(s.isSatisfied(p), true);
    }
}
