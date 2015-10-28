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

package org.btrplace.examples.migration;

import org.btrplace.examples.Example;
import org.btrplace.model.*;
import org.btrplace.model.constraint.Fence;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.migration.MinMTTRMig;
import org.btrplace.model.view.network.Network;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Vincent Kherbache
 */
public class SingleMigration implements Example {

    @Override
    public boolean run() {

        // New default model
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        // Create and boot 1 source and 1 destination node
        Node srcNode = mo.newNode(), dstNode = mo.newNode();
        ma.addOnlineNode(srcNode);
        ma.addOnlineNode(dstNode);

        // Set a default network view (connect all nodes to a main non-blocking switch through 1 Gbit/s links)
        Network.createDefaultNetwork(mo);
        
        // Create and host 1 running VM on the source node
        VM vm = mo.newVM();
        ma.addRunningVM(vm, srcNode);

        // Set the real memory used by the VM in MiB, with no workload: the VM is considered idle
        mo.getAttributes().put(vm, "memUsed", 6000); // 6 GiB

        // Add constraints
        List<SatConstraint> cstrs = new ArrayList<>();
        // We force the migration to go on the destination node
        cstrs.add(new Fence(vm, Collections.singleton(dstNode)));

        // Try to solve using the custom Min MTTR objective for migration scheduling, and show the computed plan
        try {
            ReconfigurationPlan p = new DefaultChocoScheduler().solve(mo, cstrs, new MinMTTRMig());
            System.out.println(p);
            System.out.flush();
        } catch (SchedulerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
