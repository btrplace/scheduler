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

package org.btrplace.plan;

import org.btrplace.model.*;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.event.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link DefaultReconfigurationPlanMonitor}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlanMonitorTest {

    static List<VM> vms = Util.newVMs(10);
    static List<Node> ns = Util.newNodes(10);

    static BootNode a1 = new BootNode(ns.get(2), 0, 3); //no deps
    static BootVM a2 = new BootVM(vms.get(2), ns.get(0), 0, 3); //no deps
    static MigrateVM a3 = new MigrateVM(vms.get(0), ns.get(0), ns.get(2), 4, 5); //deps: a1
    static MigrateVM a4 = new MigrateVM(vms.get(1), ns.get(1), ns.get(0), 4, 7); //no deps


    private static ReconfigurationPlan makePlan() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOfflineNode(ns.get(2));
        map.addReadyVM(vms.get(2));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(1));

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);

        plan.add(a1);
        plan.add(a3);
        plan.add(a2);
        plan.add(a4);

        Assert.assertTrue(plan.isApplyable(), '\n' + plan.toString());
        return plan;
    }

    @Test
    public void testInit() {

        ReconfigurationPlan plan = makePlan();
        ReconfigurationPlanMonitor exec = new DefaultReconfigurationPlanMonitor(plan);

        Assert.assertEquals(exec.getCurrentModel(), plan.getOrigin());
        Assert.assertFalse(exec.isBlocked(a1));
        Assert.assertFalse(exec.isBlocked(a2));
        Assert.assertTrue(exec.isBlocked(a3));
        Assert.assertFalse(exec.isBlocked(a4));
        Assert.assertEquals(exec.getNbCommitted(), 0);
    }


    @Test(dependsOnMethods = {"testInit"})
    public void testGoodCommits() {
        ReconfigurationPlan plan = makePlan();
        ReconfigurationPlanMonitor exec = new DefaultReconfigurationPlanMonitor(plan);

        Assert.assertTrue(exec.commit(a4).isEmpty());
        Assert.assertEquals(exec.getNbCommitted(), 1);
        Set<Action> released = exec.commit(a1);
        Assert.assertNotNull(released);
        Assert.assertEquals(released.size(), 1);
        Assert.assertTrue(released.contains(a3));
        Assert.assertFalse(exec.isBlocked(a3));
    }

    @Test(dependsOnMethods = {"testInit", "testGoodCommits"})
    public void testCommitBlocked() {
        ReconfigurationPlan plan = makePlan();
        ReconfigurationPlanMonitor exec = new DefaultReconfigurationPlanMonitor(plan);
        Assert.assertNull(exec.commit(a3));
    }

    @Test(dependsOnMethods = {"testInit", "testGoodCommits"})
    public void testDoubleCommit() {
        ReconfigurationPlan plan = makePlan();
        ReconfigurationPlanMonitor exec = new DefaultReconfigurationPlanMonitor(plan);
        Assert.assertNotNull(exec.commit(a1));
        Assert.assertNull(exec.commit(a1));
    }

    @Test(dependsOnMethods = {"testInit", "testGoodCommits"})
    public void testOver() {
        ReconfigurationPlan plan = makePlan();
        ReconfigurationPlanMonitor exec = new DefaultReconfigurationPlanMonitor(plan);

        Assert.assertTrue(exec.commit(a2).isEmpty());
        Assert.assertTrue(exec.commit(a4).isEmpty());
        Assert.assertFalse(exec.commit(a1).isEmpty());
        Assert.assertEquals(exec.getNbCommitted(), 3);
        Assert.assertTrue(exec.commit(a3).isEmpty());
        Assert.assertEquals(exec.getNbCommitted(), 4);
    }

    @Test
    public void testComplex() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));
        map.addOfflineNode(ns.get(3));

        map.addRunningVM(vms.get(0), ns.get(2));
        map.addRunningVM(vms.get(1), ns.get(0));
        map.addRunningVM(vms.get(2), ns.get(1));
        map.addRunningVM(vms.get(3), ns.get(1));
        BootNode bN4 = new BootNode(ns.get(3), 3, 5);
        MigrateVM mVM1 = new MigrateVM(vms.get(0), ns.get(2), ns.get(3), 6, 7);
        Allocate aVM3 = new Allocate(vms.get(2), ns.get(1), "cpu", 7, 8, 9);
        MigrateVM mVM2 = new MigrateVM(vms.get(1), ns.get(0), ns.get(1), 1, 3);
        MigrateVM mVM4 = new MigrateVM(vms.get(3), ns.get(1), ns.get(2), 1, 7);
        ShutdownNode sN1 = new ShutdownNode(ns.get(0), 5, 7);

        ShareableResource rc = new ShareableResource("cpu");
        rc.setConsumption(vms.get(2), 3);

        mo.attach(rc);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        plan.add(bN4);
        plan.add(mVM1);
        plan.add(aVM3);
        plan.add(mVM2);
        plan.add(mVM4);
        plan.add(sN1);

        Assert.assertTrue(plan.isApplyable());
    }
}
