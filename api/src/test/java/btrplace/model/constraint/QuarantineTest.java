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
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link Quarantine}.
 *
 * @author Fabien Hermenier
 */
public class QuarantineTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2));
        Quarantine q = new Quarantine(s);
        Assert.assertNotNull(q.getChecker());
        Assert.assertTrue(q.getInvolvedVMs().isEmpty());
        Assert.assertEquals(q.getInvolvedNodes(), s);
        Assert.assertTrue(q.isContinuous());
        Assert.assertFalse(q.setContinuous(false));
        Assert.assertTrue(q.setContinuous(true));
        Assert.assertFalse(q.toString().contains("null"));
//        Assert.assertEquals(q.isSatisfied(new DefaultModel(new DefaultMapping())), SatConstraint.Sat.UNDEFINED);
        System.out.println(q);
    }

    @Test
    public void testEqualsHashCode() {
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2));
        Quarantine q = new Quarantine(s);
        Assert.assertTrue(q.equals(q));
        Assert.assertTrue(q.equals(new Quarantine(new HashSet<UUID>(s))));
        Assert.assertEquals(q.hashCode(), new Quarantine(new HashSet<UUID>(s)).hashCode());
        Assert.assertFalse(q.equals(new Quarantine(new HashSet<UUID>())));
    }

    @Test
    public void testContinuousIsSatisfied() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addReadyVM(vm3);

        Quarantine q = new Quarantine(map.getOnlineNodes());

        Model mo = new DefaultModel(map);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(q.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
        plan.add(new ShutdownVM(vm2, n2, 1, 2));
        Assert.assertEquals(q.isSatisfied(plan), SatConstraint.Sat.SATISFIED);

        plan.add(new BootVM(vm3, n1, 0, 1));
        Assert.assertEquals(q.isSatisfied(plan), SatConstraint.Sat.UNSATISFIED);
    }
}
