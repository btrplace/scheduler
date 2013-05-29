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
import btrplace.plan.event.BootVM;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownVM;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link Quarantine}.
 *
 * @author Fabien Hermenier
 */
public class QuarantineTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        Set<Node> s = new HashSet<>(Arrays.asList(mo.newNode(), mo.newNode()));
        Quarantine q = new Quarantine(s);
        Assert.assertNotNull(q.getChecker());
        Assert.assertTrue(q.getInvolvedVMs().isEmpty());
        Assert.assertEquals(q.getInvolvedNodes(), s);
        Assert.assertTrue(q.isContinuous());
        Assert.assertFalse(q.setContinuous(false));
        Assert.assertTrue(q.setContinuous(true));
        Assert.assertFalse(q.toString().contains("null"));
//        Assert.assertEquals(q.isSatisfied(new DefaultModel()), SatConstraint.Sat.UNDEFINED);
        System.out.println(q);
    }

    @Test
    public void testEqualsHashCode() {
        Model mo = new DefaultModel();
        Set<Node> s = new HashSet<>(Arrays.asList(mo.newNode(), mo.newNode()));
        Quarantine q = new Quarantine(s);
        Assert.assertTrue(q.equals(q));
        Assert.assertTrue(q.equals(new Quarantine(new HashSet<>(s))));
        Assert.assertEquals(q.hashCode(), new Quarantine(new HashSet<>(s)).hashCode());
        Assert.assertFalse(q.equals(new Quarantine(new HashSet<Node>())));
    }

    @Test
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 5);
        List<Node> ns = Util.newNodes(mo, 5);
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(1));
        map.addReadyVM(vms.get(2));
        map.addRunningVM(vms.get(3), ns.get(2));

        Quarantine q = new Quarantine(new HashSet<>(Arrays.asList(ns.get(0), ns.get(1))));

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(q.isSatisfied(plan), true);
        plan.add(new ShutdownVM(vms.get(1), ns.get(1), 1, 2));
        Assert.assertEquals(q.isSatisfied(plan), true);

        plan.add(new BootVM(vms.get(2), ns.get(0), 0, 1));
        Assert.assertEquals(q.isSatisfied(plan), false);

        plan = new DefaultReconfigurationPlan(mo);
        plan.add(new BootVM(vms.get(2), ns.get(2), 0, 1));
        Assert.assertEquals(q.isSatisfied(plan), true);
        plan.add(new MigrateVM(vms.get(3), ns.get(2), ns.get(1), 0, 1));
        Assert.assertEquals(q.isSatisfied(plan), false);

        plan = new DefaultReconfigurationPlan(mo);
        plan.add(new MigrateVM(vms.get(1), ns.get(1), ns.get(0), 0, 1));
        Assert.assertEquals(q.isSatisfied(plan), false);


    }
}
