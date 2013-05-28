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
import btrplace.plan.event.Allocate;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownNode;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests {@link TimeBasedPlanApplier}.
 *
 * @author Fabien Hermenier
 */
public class TimeBasedPlanApplierTest implements PremadeElements {

    private static ReconfigurationPlan makePlan() {
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
        rc.setVMConsumption(vm3, 3);

        mo.attach(rc);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        plan.add(bN4);
        plan.add(mVM1);
        plan.add(aVM3);
        plan.add(mVM2);
        plan.add(mVM4);
        plan.add(sN1);
        return plan;
    }

    @Test
    public void testApply() {
        ReconfigurationPlan plan = makePlan();
        Model res = new DependencyBasedPlanApplier().apply(plan);
        Mapping resMapping = res.getMapping();
        Assert.assertTrue(resMapping.getOfflineNodes().contains(n1));
        Assert.assertTrue(resMapping.getOnlineNodes().contains(n4));
        ShareableResource rc = (ShareableResource) res.getView(ShareableResource.VIEW_ID_BASE + "cpu");
        Assert.assertEquals(rc.getVMConsumption(vm3), 7);
        Assert.assertEquals(resMapping.getVMLocation(vm1), n4);
        Assert.assertEquals(resMapping.getVMLocation(vm2), n2);
        Assert.assertEquals(resMapping.getVMLocation(vm4), n3);
    }

    @Test
    public void testToString() {
        ReconfigurationPlan plan = makePlan();
        Assert.assertFalse(new DependencyBasedPlanApplier().toString(plan).contains("null"));
    }
}
