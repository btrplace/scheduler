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

import java.util.List;

/**
 * Created by vkherbac on 02/09/14.
 */
public class NoDelayTest {


    @Test
    public void testIsSatisfied() {

        // Create a new default model
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();

        // Create 4 nodes
        List<Node> ns = Util.newNodes(mo, 4);

        // Create 2 vms
        List<VM> vms = Util.newVMs(mo, 2);

        // Set the nodes online
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));
        map.addOnlineNode(ns.get(3));

        // Run the 2 vms on the two first nodes
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(1));

        // Set as initial plan
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);

        // Create a NoDelay constraint by constraining the first VM
        NoDelay nd = new NoDelay(vms.get(0));

        // The constraint should be satisfied by default
        Assert.assertEquals(nd.isSatisfied(plan), true);

        // Migrate the first VM (constrained) at t=0 to the third node
        plan.add(new MigrateVM(vms.get(0), ns.get(0), ns.get(2), 0, 1));
        Assert.assertEquals(nd.isSatisfied(plan), true);

        // Migrate the second VM at t=0 to the last node
        plan.add(new MigrateVM(vms.get(1), ns.get(1), ns.get(3), 0, 1));
        Assert.assertEquals(nd.isSatisfied(plan), true);

        // Re-Migrate the second VM at t=1 to the second node
        plan.add(new MigrateVM(vms.get(1), ns.get(3), ns.get(1), 1, 2));
        Assert.assertEquals(nd.isSatisfied(plan), true);

        // Re-Migrate the first VM (constrained) at t=1 to the first node
        plan.add(new MigrateVM(vms.get(0), ns.get(2), ns.get(0), 1, 2));
        Assert.assertEquals(nd.isSatisfied(plan), false);


        // Shutdown node
        //plan.add(new ShutdownNode(ns.get(2), 0, 1));
        //Assert.assertEquals(nd.isSatisfied(plan), true);

        // Boot node
        //plan.add(new BootNode(ns.get(2), 1, 3));
        //Assert.assertEquals(nd.isSatisfied(plan), false);
    }
}
