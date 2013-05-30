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
 * Unit tests for {@link SplitAmong}.
 *
 * @author Fabien Hermenier
 */
public class SplitAmongTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 5);
        List<VM> vms = Util.newVMs(mo, 5);

        Set<VM> vs1 = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1)));
        Set<VM> vs2 = new HashSet<>(Arrays.asList(vms.get(2), vms.get(3)));

        Set<Set<VM>> vGrps = new HashSet<>(Arrays.asList(vs1, vs2));


        Set<Node> ps1 = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        Set<Node> ps2 = new HashSet<>(Arrays.asList(ns.get(2), ns.get(3)));
        Set<Set<Node>> pGrps = new HashSet<>(Arrays.asList(ps1, ps2));

        SplitAmong sp = new SplitAmong(vGrps, pGrps);
        Assert.assertNotNull(sp.getChecker());
        Assert.assertEquals(sp.getGroupsOfVMs(), vGrps);
        Assert.assertEquals(sp.getGroupsOfNodes(), pGrps);
        Assert.assertTrue(sp.getInvolvedVMs().containsAll(vs1));
        Assert.assertTrue(sp.getInvolvedVMs().containsAll(vs2));
        Assert.assertTrue(sp.getInvolvedNodes().containsAll(ps1));
        Assert.assertTrue(sp.getInvolvedNodes().containsAll(ps2));
        System.out.println(sp.toString());

        Assert.assertFalse(sp.isContinuous());
        Assert.assertTrue(sp.setContinuous(true));
        Assert.assertTrue(sp.isContinuous());

        Assert.assertTrue(sp.setContinuous(false));
        Assert.assertFalse(sp.isContinuous());

        sp = new SplitAmong(vGrps, pGrps, true);
        Assert.assertTrue(sp.isContinuous());
    }

    @Test
    public void testEqualsAndHashCode() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 5);
        List<VM> vms = Util.newVMs(mo, 5);

        Set<VM> vs1 = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1)));
        Set<VM> vs2 = new HashSet<>(Arrays.asList(vms.get(2), vms.get(3)));
        Set<Set<VM>> vGrps = new HashSet<>(Arrays.asList(vs1, vs2));


        Set<Node> ps1 = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        Set<Node> ps2 = new HashSet<>(Arrays.asList(ns.get(2), ns.get(3)));
        Set<Set<Node>> pGrps = new HashSet<>(Arrays.asList(ps1, ps2));

        SplitAmong sp = new SplitAmong(vGrps, pGrps);
        Assert.assertTrue(sp.equals(sp));
        Assert.assertTrue(sp.equals(new SplitAmong(vGrps, pGrps)));
        Assert.assertEquals(sp.hashCode(), new SplitAmong(vGrps, pGrps).hashCode());
        Assert.assertFalse(sp.equals(new SplitAmong(new HashSet<Set<VM>>(), pGrps)));
        Assert.assertFalse(sp.equals(new SplitAmong(vGrps, new HashSet<Set<Node>>())));

        SplitAmong sp2 = new SplitAmong(vGrps, pGrps);
        sp2.setContinuous(true);
        Assert.assertFalse(sp.equals(sp2));
    }

    @Test
    public void testDiscreteIsSatisfied() {

        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 5);
        List<VM> vms = Util.newVMs(mo, 5);

        Set<VM> vs1 = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1)));
        Set<VM> vs2 = new HashSet<>(Arrays.asList(vms.get(2), vms.get(3)));
        Set<Set<VM>> vGrps = new HashSet<>(Arrays.asList(vs1, vs2));


        Set<Node> ps1 = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        Set<Node> ps2 = new HashSet<>(Arrays.asList(ns.get(2), ns.get(3)));
        Set<Set<Node>> pGrps = new HashSet<>(Arrays.asList(ps1, ps2));

        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));
        map.addOnlineNode(ns.get(3));

        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(0));
        map.addRunningVM(vms.get(2), ns.get(2));
        map.addRunningVM(vms.get(3), ns.get(3));

        SplitAmong sp = new SplitAmong(vGrps, pGrps);
        Assert.assertEquals(sp.isSatisfied(mo), true);

        //Spread over multiple groups, not allowed
        map.addRunningVM(vms.get(1), ns.get(2));
        Assert.assertEquals(sp.isSatisfied(mo), false);
        //pGroup co-location. Not allowed
        map.addRunningVM(vms.get(0), ns.get(2));
        map.addRunningVM(vms.get(2), ns.get(3));
        Assert.assertEquals(sp.isSatisfied(mo), false);
    }

    @Test
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 5);
        List<VM> vms = Util.newVMs(mo, 5);


        Set<VM> vs1 = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1)));
        Set<VM> vs2 = new HashSet<>(Arrays.asList(vms.get(2), vms.get(3)));
        Set<Set<VM>> vGrps = new HashSet<>(Arrays.asList(vs1, vs2));


        Set<Node> ps1 = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        Set<Node> ps2 = new HashSet<>(Arrays.asList(ns.get(2), ns.get(3)));
        Set<Set<Node>> pGrps = new HashSet<>(Arrays.asList(ps1, ps2));

        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));
        map.addOnlineNode(ns.get(3));

        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(0));
        map.addRunningVM(vms.get(2), ns.get(2));
        map.addRunningVM(vms.get(3), ns.get(3));

        SplitAmong sp = new SplitAmong(vGrps, pGrps, true);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(sp.isSatisfied(plan), true);

        plan.add(new MigrateVM(vms.get(0), ns.get(0), ns.get(1), 3, 4));
        Assert.assertEquals(sp.isSatisfied(plan), true);

        map.addRunningVM(vms.get(4), ns.get(3));
        Assert.assertEquals(sp.isSatisfied(plan), true);
        plan.add(new MigrateVM(vms.get(1), ns.get(0), ns.get(2), 0, 2));
        Assert.assertEquals(sp.isSatisfied(plan), false);


    }
}
