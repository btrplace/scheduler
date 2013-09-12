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
import btrplace.plan.event.ShutdownNode;
import btrplace.plan.event.ShutdownVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link btrplace.model.constraint.Offline}.
 *
 * @author Fabien Hermenier
 */
public class OfflineTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        Set<Node> s = new HashSet<>(Arrays.asList(mo.newNode(), mo.newNode()));
        Offline o = new Offline(s);
        Assert.assertNotNull(o.getChecker());
        Assert.assertEquals(o.getInvolvedNodes(), s);
        Assert.assertTrue(o.getInvolvedVMs().isEmpty());
        Assert.assertNotNull(o.toString());
        Assert.assertFalse(o.setContinuous(true));
        System.out.println(o);
    }

    @Test
    public void testIsSatisfied() {
        Model i = new DefaultModel();
        Mapping c = i.getMapping();
        Node n1 = i.newNode();
        Node n2 = i.newNode();
        c.addOfflineNode(n1);
        c.addOfflineNode(n2);
        Set<Node> s = new HashSet<>(Arrays.asList(n1, n2));
        Offline o = new Offline(s);

        Assert.assertEquals(o.isSatisfied(i), true);
        c.addOnlineNode(n2);
        Assert.assertEquals(o.isSatisfied(i), false);
    }

    @Test
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);
        Mapping map = mo.getMapping();

        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));

        Set<Node> s = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        Offline off = new Offline(s);

        map.addRunningVM(vms.get(0), ns.get(0));

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(off.isSatisfied(plan), false);
        plan.add(new ShutdownNode(ns.get(1), 0, 1));
        plan.add(new ShutdownVM(vms.get(0), ns.get(0), 0, 1));
        Assert.assertEquals(off.isSatisfied(plan), false);
        plan.add(new ShutdownNode(ns.get(0), 1, 2));
        Assert.assertEquals(off.isSatisfied(plan), true);

    }

    @Test
    public void testEquals() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);

        Set<Node> x = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        Offline s = new Offline(x);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Offline(x).equals(s));
        Assert.assertEquals(new Offline(x).hashCode(), s.hashCode());
        x = new HashSet<>(Arrays.asList(ns.get(2)));
        Assert.assertFalse(new Offline(x).equals(s));
    }
}
