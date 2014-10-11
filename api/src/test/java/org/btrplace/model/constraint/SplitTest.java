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
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SuspendVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link Split}.
 *
 * @author Fabien Hermenier
 */
public class SplitTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 3);
        List<VM> vms = Util.newVMs(mo, 3);

        Collection<VM> s1 = Collections.singleton(vms.get(0));
        Collection<VM> s2 = Collections.singleton(vms.get(1));
        List<Collection<VM>> args = Arrays.asList(s1, s2);
        Split sp = new Split(args);
        Assert.assertNotNull(sp.getChecker());
        Assert.assertEquals(args, sp.getSets());
        Assert.assertEquals(2, sp.getInvolvedVMs().size());
        Assert.assertTrue(sp.getInvolvedNodes().isEmpty());
        Assert.assertFalse(sp.toString().contains("null"));
        Assert.assertFalse(sp.isContinuous());
        Assert.assertTrue(sp.setContinuous(true));
        Assert.assertTrue(sp.isContinuous());
        Assert.assertTrue(sp.setContinuous(false));
        Assert.assertFalse(sp.isContinuous());
        System.out.println(sp);

        sp = new Split(args, true);
        Assert.assertTrue(sp.isContinuous());
    }

    @Test
    public void testEquals() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 3);
        List<VM> vms = Util.newVMs(mo, 3);

        Collection<VM> s1 = Collections.singleton(vms.get(0));
        Collection<VM> s2 = Collections.singleton(vms.get(1));
        List<Collection<VM>> args = Arrays.asList(s1, s2);
        Split sp = new Split(args);
        Assert.assertTrue(sp.equals(sp));
        Assert.assertTrue(new Split(args).equals(sp));
        Assert.assertEquals(new Split(args).hashCode(), sp.hashCode());
        List<Collection<VM>> args2 = new ArrayList<>(args);
        args2.add(Collections.singleton(vms.get(2)));
        Assert.assertFalse(new Split(args2).equals(sp));
        Assert.assertNotEquals(new Split(args, true), new Split(args, false));
    }

    @Test
    public void testDiscreteIsSatisfied() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 3);
        List<VM> vms = Util.newVMs(mo, 5);

        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));

        Collection<VM> s1 = Arrays.asList(vms.get(0), vms.get(1));
        Collection<VM> s2 = Arrays.asList(vms.get(2), vms.get(3));
        Collection<VM> s3 = Collections.singleton(vms.get(4));
        Collection<Collection<VM>> args = Arrays.asList(s1, s2, s3);

        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(0));
        map.addRunningVM(vms.get(2), ns.get(1));
        map.addRunningVM(vms.get(3), ns.get(1));

        Split sp = new Split(args);
        Assert.assertEquals(sp.isSatisfied(mo), true);
        map.addRunningVM(vms.get(2), ns.get(2));
        Assert.assertEquals(sp.isSatisfied(mo), true);
        map.addRunningVM(vms.get(2), ns.get(0));
        Assert.assertEquals(sp.isSatisfied(mo), false);
    }

    @Test
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        List<Node> ns = Util.newNodes(mo, 3);
        List<VM> vms = Util.newVMs(mo, 5);

        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));

        Collection<VM> s1 = Arrays.asList(vms.get(0), vms.get(1));
        Collection<VM> s2 = Arrays.asList(vms.get(2), vms.get(3));
        Collection<VM> s3 = Collections.singleton(vms.get(4));
        Collection<Collection<VM>> args = Arrays.asList(s1, s2, s3);

        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(0));
        map.addRunningVM(vms.get(2), ns.get(1));
        map.addRunningVM(vms.get(3), ns.get(1));

        Split sp = new Split(args, true);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(sp.isSatisfied(plan), true);
        map.addRunningVM(vms.get(2), ns.get(0)); //Violation
        Assert.assertEquals(sp.isSatisfied(plan), false);

        plan.add(new MigrateVM(vms.get(2), ns.get(0), ns.get(1), 0, 1));
        Assert.assertEquals(sp.isSatisfied(plan), false); //False cause there is the initial violation
        sp.setContinuous(false);
        Assert.assertEquals(sp.isSatisfied(plan), true);

        sp.setContinuous(true);
        //Temporary overlap
        plan.add(new MigrateVM(vms.get(2), ns.get(1), ns.get(0), 5, 6));
        plan.add(new MigrateVM(vms.get(2), ns.get(0), ns.get(1), 6, 7));
        Assert.assertEquals(sp.isSatisfied(plan), false);

        //Liberate ns.get(0) from vms.get(0) and vms.get(1) before
        plan.add(new SuspendVM(vms.get(0), ns.get(0), ns.get(0), 2, 3));
        plan.add(new ShutdownVM(vms.get(1), ns.get(0), 2, 3));
        sp.setContinuous(false);
        Assert.assertEquals(sp.isSatisfied(plan), true);
    }
}
