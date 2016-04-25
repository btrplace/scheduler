/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
import org.btrplace.model.constraint.Online;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.migration.Deadline;
import org.btrplace.model.constraint.migration.Precedence;
import org.btrplace.model.constraint.migration.Serialize;
import org.btrplace.model.constraint.migration.Sync;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.Switch;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author Vincent Kherbache
 */
public class AdvancedMigScheduling implements Example {

    private Node srcNode1;
    private Node srcNode2;
    private Node srcNode3;
    private Node srcNode4;
    private Node dstNode1;
    private Node dstNode2;

    private VM vm0;
    private VM vm1;
    private VM vm2;
    private VM vm3;


    private Model makeModel() {
        // New default model
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        // Create and boot 4 source nodes and 2 destination nodes
        srcNode1 = mo.newNode();
        srcNode2 = mo.newNode();
        srcNode3 = mo.newNode();
        srcNode4 = mo.newNode();
        dstNode1 = mo.newNode();
        dstNode2 = mo.newNode();

        ma.addOnlineNode(srcNode1);
        ma.addOnlineNode(srcNode2);
        ma.addOnlineNode(srcNode3);
        ma.addOnlineNode(srcNode4);
        ma.addOfflineNode(dstNode1);
        ma.addOfflineNode(dstNode2);

        // Create and host 1 VM per source node
        vm0 = mo.newVM();
        vm1 = mo.newVM();
        vm2 = mo.newVM();
        vm3 = mo.newVM();
        ma.addRunningVM(vm0, srcNode1);
        ma.addRunningVM(vm1, srcNode2);
        ma.addRunningVM(vm2, srcNode3);
        ma.addRunningVM(vm3, srcNode4);
        return mo;
    }

    @Override
    public boolean run() {

        Model mo = makeModel();

        // Create, define, and attach CPU and Mem resource views for nodes and VMs
        int mem_src = 8;
        int cpu_src = 4;
        int mem_dst = 16;
        int cpu_dst = 8;
        ShareableResource rcMem = new ShareableResource("mem", 0, 8);
        ShareableResource rcCPU = new ShareableResource("cpu", 0, 4);
        // VMs resources consumption
        // Nodes resources capacity
        rcMem.setCapacity(srcNode1, mem_src)
                .setCapacity(srcNode2, mem_src)
                .setCapacity(srcNode3, mem_src)
                .setCapacity(srcNode4, mem_src)
                .setCapacity(dstNode1, mem_dst)
                .setCapacity(dstNode2, mem_dst);
        rcCPU.setCapacity(srcNode1, cpu_src)
                .setCapacity(srcNode2, cpu_src)
                .setCapacity(srcNode3, cpu_src)
                .setCapacity(srcNode4, cpu_src)
                .setCapacity(dstNode1, cpu_dst)
                .setCapacity(dstNode2, cpu_dst);
        mo.attach(rcMem);
        mo.attach(rcCPU);

        // Set VM attributes 'hot dirty page size', 'hot dirty page duration', and 'cold dirty pages rate'
        // to simulate a memory intensive workload equivalent to "stress --vm 1000 --bytes 50K"
        int vm_hds = 56;
        int vm_hdd = 2;
        double vm_cdr = 22.6;
        // vm0 is an 'idle' VM (with no special memory activity) but still consumes 2 GiB of memory
        mo.getAttributes().put(vm0, "memUsed", 2000);
        // vm1 is an 'idle' VM (with no special memory activity) but still consumes 4 GiB of memory
        mo.getAttributes().put(vm1, "memUsed", 3000);
        // vm2 consumes 4 GiB memory and has a memory intensive workload
        mo.getAttributes().put(vm2, "memUsed", 4000);
        mo.getAttributes().put(vm2, "hotDirtySize", vm_hds);
        mo.getAttributes().put(vm2, "hotDirtyDuration", vm_hdd);
        mo.getAttributes().put(vm2, "coldDirtyRate", vm_cdr);
        // vm3 consumes 6 GiB memory and has a memory intensive workload
        mo.getAttributes().put(vm3, "memUsed", 5000);
        mo.getAttributes().put(vm3, "hotDirtySize", vm_hds);
        mo.getAttributes().put(vm3, "hotDirtyDuration", vm_hdd);
        mo.getAttributes().put(vm3, "coldDirtyRate", vm_cdr);

        // Attach a network view
        Network net = new Network();
        mo.attach(net);
        Switch swMain = net.newSwitch(30000);
        net.connect(10000, swMain, srcNode1, srcNode2, srcNode3, srcNode4);
        // The destination nodes have twice the bandwidth of source nodes
        net.connect(20000, swMain, dstNode1, dstNode2);

        // Create constraints
        List<SatConstraint> cstrs = new ArrayList<>();
        // We want to boot the destination nodes
        cstrs.addAll(Online.newOnline(Arrays.asList(dstNode1, dstNode2)));
        // We want to shutdown the source nodes
        cstrs.addAll(Offline.newOffline(Arrays.asList(srcNode1, srcNode2, srcNode3, srcNode4)));

        // Try to solve as is, and show the computed plan
        if (!solve(mo, cstrs)) {
            return false;
        }

        /********* Add some migrations scheduling constraints *********/

        // We want vm0 and vm1 migrations to terminate at the same time
        cstrs.add(new Sync(vm0, vm1));

        // We want to serialize the migrations of vm1, vm2, and vm3
        cstrs.add(new Serialize(new HashSet<>(Arrays.asList(vm1, vm2, vm3))));
        
        // We want vm0 migration terminate before vm2 start to migrate
        cstrs.add(new Precedence(vm1, vm2));

        // We want vm3 migration terminate before 10s
        cstrs.add(new Deadline(vm3, "+0:0:10"));

        // Try to solve, and show the computed plan
        return solve(mo, cstrs);
    }

    private static boolean solve(Model mo, List<SatConstraint> cstrs) {
        ReconfigurationPlan p = new DefaultChocoScheduler().solve(mo, cstrs);
        if (p == null) {
            return false;
        }
        System.out.println(p);
        System.out.flush();
        return true;
    }
}
