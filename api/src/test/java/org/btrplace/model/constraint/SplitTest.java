/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
        Util.newNodes(mo, 3);
        List<VM> vms = Util.newVMs(mo, 3);

        Collection<VM> s1 = Collections.singleton(vms.get(0));
        Collection<VM> s2 = Collections.singleton(vms.get(1));
        List<Collection<VM>> args = Arrays.asList(s1, s2);
        Split sp = new Split(args);
        Assert.assertNotNull(sp.getChecker());
        Assert.assertEquals(args, sp.getSets());
        Assert.assertEquals(sp.getInvolvedVMs().size(), 2);
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
        Util.newNodes(mo, 3);
        List<VM> vms = Util.newVMs(mo, 3);

        Collection<VM> s1 = Collections.singleton(vms.get(0));
        Collection<VM> s2 = Collections.singleton(vms.get(1));
        List<Collection<VM>> args = Arrays.asList(s1, s2);
        Split sp = new Split(args);
        Assert.assertEquals(sp, sp);
        Assert.assertEquals(sp, new Split(args));
        Assert.assertEquals(new Split(args).hashCode(), sp.hashCode());
        List<Collection<VM>> args2 = new ArrayList<>(args);
        args2.add(Collections.singleton(vms.get(2)));
        Assert.assertNotEquals(sp, new Split(args2));
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
        Assert.assertTrue(sp.isSatisfied(mo));
        map.addRunningVM(vms.get(2), ns.get(2));
        Assert.assertTrue(sp.isSatisfied(mo));
        map.addRunningVM(vms.get(2), ns.get(0));
        Assert.assertFalse(sp.isSatisfied(mo));
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
        Assert.assertTrue(sp.isSatisfied(plan));
        map.addRunningVM(vms.get(2), ns.get(0)); //Violation
        Assert.assertFalse(sp.isSatisfied(plan));

        plan.add(new MigrateVM(vms.get(2), ns.get(0), ns.get(1), 0, 1));
        Assert.assertFalse(sp.isSatisfied(plan)); //False cause there is the initial violation
        sp.setContinuous(false);
        Assert.assertTrue(sp.isSatisfied(plan));

        sp.setContinuous(true);
        //Temporary overlap
        plan.add(new MigrateVM(vms.get(2), ns.get(1), ns.get(0), 5, 6));
        plan.add(new MigrateVM(vms.get(2), ns.get(0), ns.get(1), 6, 7));
        Assert.assertFalse(sp.isSatisfied(plan));

        //Liberate ns.get(0) from vms.get(0) and vms.get(1) before
        plan.add(new SuspendVM(vms.get(0), ns.get(0), ns.get(0), 2, 3));
        plan.add(new ShutdownVM(vms.get(1), ns.get(0), 2, 3));
        sp.setContinuous(false);
        Assert.assertTrue(sp.isSatisfied(plan));
    }
}
