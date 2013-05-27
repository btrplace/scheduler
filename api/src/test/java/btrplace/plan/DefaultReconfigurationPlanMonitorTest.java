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

package btrplace.plan;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.view.ShareableResource;
import btrplace.plan.event.*;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;

/**
 * Unit tests for {@link DefaultReconfigurationPlanMonitor}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlanMonitorTest implements PremadeElements {

    static BootNode a1 = new BootNode(n3, 0, 3); //no deps
    static BootVM a2 = new BootVM(vm3, n1, 0, 3); //no deps
    static MigrateVM a3 = new MigrateVM(vm1, n1, n3, 4, 5); //deps: a1
    static MigrateVM a4 = new MigrateVM(vm2, n2, n1, 4, 7); //no deps


    private static ReconfigurationPlan makePlan() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);
        map.addReadyVM(vm3);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);

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
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOfflineNode(n4);

        map.addRunningVM(vm1, n3);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n2);
        BootNode bN4 = new BootNode(n4, 3, 5);
        MigrateVM mVM1 = new MigrateVM(vm1, n3, n4, 6, 7);
        Allocate aVM3 = new Allocate(vm3, n2, "cpu", 7, 8, 9);
        MigrateVM mVM2 = new MigrateVM(vm2, n1, n2, 1, 3);
        MigrateVM mVM4 = new MigrateVM(vm4, n2, n3, 1, 7);
        ShutdownNode sN1 = new ShutdownNode(n1, 5, 7);

        ShareableResource rc = new ShareableResource("cpu");
        rc.set(vm3, 3);

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
