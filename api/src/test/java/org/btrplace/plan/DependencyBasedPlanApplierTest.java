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
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.BootNode;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ShutdownNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Unit tests for {@link DependencyBasedPlanApplier}.
 *
 * @author Fabien Hermenier
 */
public class DependencyBasedPlanApplierTest {

    @Test
    public void testApply() {

        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 10);
        List<Node> ns = Util.newNodes(mo, 10);

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


        Model res = new DependencyBasedPlanApplier().apply(plan);
        Assert.assertNotNull(res);
        Mapping resMapping = res.getMapping();
        Assert.assertTrue(resMapping.isOffline(ns.get(0)));
        Assert.assertTrue(resMapping.isOnline(ns.get(3)));
        rc = (ShareableResource) res.getView(ShareableResource.VIEW_ID_BASE + "cpu");
        Assert.assertEquals(rc.getConsumption(vms.get(2)), 7);
        Assert.assertEquals(resMapping.getVMLocation(vms.get(0)), ns.get(3));
        Assert.assertEquals(resMapping.getVMLocation(vms.get(1)), ns.get(1));
        Assert.assertEquals(resMapping.getVMLocation(vms.get(3)), ns.get(2));
    }
}
