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
import org.btrplace.model.constraint.Offline;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.migration.Precedence;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.Switch;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.DefaultParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vincent Kherbache
 */
public class ScheduleMigrations implements Example {

    @Override
    public boolean run() {

        // New default model
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        // Create and boot 2 source nodes and 1 destination node
        Node srcNode1 = mo.newNode(), srcNode2 = mo.newNode(), dstNode = mo.newNode();
        ma.addOnlineNode(srcNode1);
        ma.addOnlineNode(srcNode2);
        ma.addOnlineNode(dstNode);

        // Attach a network view
        Network net = new Network();
        mo.attach(net);
        // Connect the nodes through a main non-blocking switch
        // The destination node has twice the bandwidth of source nodes
        Switch swMain = net.newSwitch();
        net.connect(1000, swMain, srcNode1, srcNode2);
        net.connect(2000, swMain, dstNode);
        
        // Create and host 1 VM per source node
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        ma.addRunningVM(vm1, srcNode1);
        ma.addRunningVM(vm2, srcNode2);

        // Set VM attributes 'memory used', 'hot dirty page size', 'hot dirty page duration' and 'cold dirty pages rate'
        int vm_mu = 6000, vm_hds = 46, vm_hdd = 2; double vm_cdr = 23.6;
        // vm1 is an 'idle' VM (with no special memory activity) but still consumes 6 GiB of memory
        mo.getAttributes().put(vm1, "memUsed", vm_mu);
        // vm2 consumes 6 GiB memory and has a memory intensive workload equivalent to "stress --vm 1000 --bytes 50K"
        mo.getAttributes().put(vm2, "memUsed", vm_mu);
        mo.getAttributes().put(vm2, "hotDirtySize", vm_hds);
        mo.getAttributes().put(vm2, "hotDirtyDuration", vm_hdd);
        mo.getAttributes().put(vm2, "coldDirtyRate", vm_cdr);
                
        // Create constraints
        List<SatConstraint> cstrs = new ArrayList<>();
        // Placement constraints: we want to shutdown the source nodes to force VMs migration to destination nodes
        cstrs.add(new Offline(srcNode1));
        cstrs.add(new Offline(srcNode2));
        // Scheduling constraint: we want vm2 start before vm1 for example (try: Sync, Serialize, Deadline, ..)
        cstrs.add(new Precedence(vm2, vm1));

        // Set parameter: /!\ Optimize the migrations scheduling /!\
        DefaultParameters ps = new DefaultParameters();
        ps.doOptimizeMigScheduling(true);

        // Try to solve, and show the computed plan
        try {
            ReconfigurationPlan p = new DefaultChocoScheduler(ps).solve(mo, cstrs);
            System.out.println(p);
            System.out.flush();
            
        } catch (SchedulerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
