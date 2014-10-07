/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.*;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.plan.event.ShutdownVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Unit tests for {@link org.btrplace.model.constraint.Offline}.
 *
 * @author Fabien Hermenier
 */
public class OfflineTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        Node n = mo.newNode();
        Offline o = new Offline(n);
        Assert.assertNotNull(o.getChecker());
        Assert.assertTrue(o.getInvolvedNodes().contains(n));
        Assert.assertTrue(o.getInvolvedVMs().isEmpty());
        Assert.assertNotNull(o.toString());
        Assert.assertTrue(o.setContinuous(true));
        System.out.println(o);
    }

    @Test
    public void testIsSatisfied() {
        Model i = new DefaultModel();
        Mapping c = i.getMapping();
        Node n1 = i.newNode();
        Node n2 = i.newNode();
        c.addOfflineNode(n1);
        Offline o = new Offline(n1);

        Assert.assertEquals(o.isSatisfied(i), true);
        c.addOnlineNode(n2);
        Assert.assertEquals(new Offline(n2).isSatisfied(i), false);
    }

    @Test
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);
        Mapping map = mo.getMapping();

        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));

        Offline off = new Offline(ns.get(0));

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

        Offline s = new Offline(ns.get(0));

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Offline(ns.get(0)).equals(s));
        Assert.assertEquals(new Offline(ns.get(0)).hashCode(), s.hashCode());
        Assert.assertFalse(new Offline(ns.get(1)).equals(s));
    }
}
