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
import org.btrplace.plan.event.MigrateVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link Ban}.
 *
 * @author Fabien Hermenier
 */
public class BanTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        Set<Node> nodes = new HashSet<>(Arrays.asList(mo.newNode()));
        VM v = mo.newVM();
        Ban b = new Ban(v, nodes);
        Assert.assertTrue(b.getInvolvedVMs().contains(v));
        Assert.assertEquals(nodes, b.getInvolvedNodes());
        Assert.assertFalse(b.toString().contains("null"));
        Assert.assertTrue(b.setContinuous(true));
        Assert.assertNotNull(b.getChecker());
        System.out.println(b);
    }

    @Test
    public void testIsSatisfied() {

        Model m = new DefaultModel();
        List<VM> vms = Util.newVMs(m, 10);
        List<Node> ns = Util.newNodes(m, 10);
        Mapping map = m.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));

        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(1));
        map.addRunningVM(vms.get(2), ns.get(2));
        Set<Node> nodes = new HashSet<>(Arrays.asList(ns.get(0)));

        Ban b = new Ban(vms.get(2), nodes);
        Assert.assertEquals(b.isSatisfied(m), true);
        map.addRunningVM(vms.get(2), ns.get(0));
        Assert.assertEquals(new Ban(vms.get(2), nodes).isSatisfied(m), false);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(m);
        plan.add(new MigrateVM(vms.get(0), ns.get(0), ns.get(1), 0, 3));
        plan.add(new MigrateVM(vms.get(0), ns.get(1), ns.get(0), 3, 6));
        plan.add(new MigrateVM(vms.get(1), ns.get(1), ns.get(2), 3, 6));
        Assert.assertEquals(b.isSatisfied(plan), false);
    }

    @Test
    public void testEquals() {
        Model m = new DefaultModel();
        VM v = m.newVM();
        List<Node> ns = Util.newNodes(m, 10);

        Set<Node> nodes = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));

        Ban b = new Ban(v, nodes);
        Assert.assertTrue(b.equals(b));
        Assert.assertTrue(new Ban(v, nodes).equals(b));
        Assert.assertEquals(new Ban(v, nodes).hashCode(), b.hashCode());
        Assert.assertNotEquals(new Ban(m.newVM(), nodes), b);
    }
}
