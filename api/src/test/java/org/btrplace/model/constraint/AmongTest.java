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

import java.util.*;

/**
 * Unit tests for {@link Among}.
 *
 * @author Fabien Hermenier
 */
public class AmongTest {

    @Test
    public void testInstantiation() {

        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);

        Collection<Node> s1 = Arrays.asList(ns.get(0), ns.get(1));
        Collection<Node> s2 = Arrays.asList(ns.get(2));
        Collection<Collection<Node>> pGrps = Arrays.asList(s1, s2);
        Set<VM> vg = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1), vms.get(2)));
        Among a = new Among(vg, pGrps);
        Assert.assertNotNull(a.getChecker());
        Assert.assertEquals(a.getInvolvedVMs(), vg);
        Assert.assertEquals(a.getGroupsOfNodes(), pGrps);
        Assert.assertEquals(a.getInvolvedNodes().size(), s1.size() + s2.size());
        Assert.assertTrue(a.getInvolvedNodes().containsAll(s1));
        Assert.assertTrue(a.getInvolvedNodes().containsAll(s2));
        Assert.assertFalse(a.toString().contains("null"));
        Assert.assertFalse(a.isContinuous());
        Assert.assertTrue(a.setContinuous(true));
        Assert.assertTrue(a.setContinuous(false));

        a = new Among(vg, pGrps, true);
        Assert.assertTrue(a.isContinuous());
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsHashCode() {

        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);


        Collection<Node> s1 = Arrays.asList(ns.get(0), ns.get(1));
        Collection<Node> s2 = Arrays.asList(ns.get(2));
        Collection<Collection<Node>> pGrps = Arrays.asList(s1, s2);
        Set<VM> vg = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1), vms.get(2)));

        Among a = new Among(vg, pGrps);
        Assert.assertTrue(a.equals(a));
        Assert.assertTrue(a.equals(new Among(new HashSet<>(vg), pGrps)));
        Assert.assertEquals(a.hashCode(), new Among(new HashSet<>(vg), pGrps).hashCode());
        Assert.assertFalse(a.equals(new Among(new HashSet<VM>(), pGrps)));
        Assert.assertFalse(a.equals(new Among(new HashSet<>(vg), Collections.<Collection<Node>>emptyList())));
        Among a2 = new Among(new HashSet<>(vg), Collections.<Collection<Node>>emptyList());
        a2.setContinuous(true);
        Assert.assertFalse(a.equals(a2));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testDiscreteIsSatisfied() {

        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);

        Collection<Node> s1 = Arrays.asList(ns.get(0), ns.get(1));
        Collection<Node> s2 = Arrays.asList(ns.get(2));
        Collection<Collection<Node>> pGrps = new HashSet<>(Arrays.asList(s1, s2));
        Set<VM> vs = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1), vms.get(2)));

        Among a = new Among(vs, pGrps);

        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(1));
        map.addSleepingVM(vms.get(2), ns.get(2));

        Assert.assertEquals(a.isSatisfied(mo), true);
        map.addRunningVM(vms.get(2), ns.get(2));
        Assert.assertEquals(a.isSatisfied(mo), false);
        map.addSleepingVM(vms.get(2), ns.get(1));
        Assert.assertEquals(a.isSatisfied(mo), true);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testContinuousIsSatisfied() {

        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);


        Collection<Node> s1 = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        Collection<Node> s2 = new HashSet<>(Arrays.asList(ns.get(2)));
        Collection<Collection<Node>> pGrps = Arrays.asList(s1, s2);
        Set<VM> vs = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1), vms.get(2)));

        Among a = new Among(vs, pGrps, true);


        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(1));
        map.addRunningVM(vms.get(2), ns.get(1));

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(a.isSatisfied(plan), true);

        plan.add(new MigrateVM(vms.get(2), ns.get(1), ns.get(2), 0, 1));
        plan.add(new MigrateVM(vms.get(2), ns.get(2), ns.get(1), 1, 2));
        //At moment 1, the constraint will be violated
        Assert.assertEquals(a.isSatisfied(plan), false);
    }
}
