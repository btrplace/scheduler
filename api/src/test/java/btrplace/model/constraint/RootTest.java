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

import java.util.*;

/**
 * Unit tests for {@link btrplace.model.constraint.Root}.
 *
 * @author Fabien Hermenier
 */
public class RootTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<UUID> x = new HashSet<UUID>(Arrays.asList(vm1, vm2));
        Root s = new Root(x);
        Assert.assertEquals(x, s.getInvolvedVMs());
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertNotNull(s.toString());
        System.out.println(s);
        Assert.assertTrue(s.isContinuous());
        Assert.assertFalse(s.setContinuous(false));
        Assert.assertTrue(s.isContinuous());
        Assert.assertTrue(s.setContinuous(true));
        Assert.assertTrue(s.isContinuous());
    }

    @Test
    public void testEquals() {
        Set<UUID> x = new HashSet<UUID>(Arrays.asList(vm1, vm2));
        Root s = new Root(x);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Root(x).equals(s));
        Assert.assertEquals(s.hashCode(), new Root(x).hashCode());
        x = Collections.singleton(vm3);
        Assert.assertFalse(new Root(x).equals(s));
    }

    @Test
    public void testIsSatisfied() {
        Mapping c = new DefaultMapping();
        c.addReadyVM(n1);
        c.addReadyVM(n2);
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2));
        Root o = new Root(s);

        Model i = new DefaultModel(c);

        Assert.assertEquals(o.isSatisfied(i), SatConstraint.Sat.SATISFIED);
        c.clear();
        Assert.assertEquals(o.isSatisfied(i), SatConstraint.Sat.SATISFIED);
    }

    @Test
    public void testContinuousIsSatisfied() {
        Mapping map = new DefaultMapping();
        Model mo = new DefaultModel(map);
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        Root r = new Root(Collections.singleton(vm1));
        Assert.assertEquals(r.isSatisfied(p), SatConstraint.Sat.SATISFIED);
        p.add(new MigrateVM(vm1, n1, n2, 1, 2));
        Assert.assertEquals(r.isSatisfied(p), SatConstraint.Sat.UNSATISFIED);
    }

}
