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

package org.btrplace.scheduler.choco.constraint.migration;

import org.btrplace.model.*;
import org.btrplace.model.constraint.Offline;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.migration.MinMTTRMig;
import org.btrplace.model.constraint.migration.Precedence;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.Switch;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link org.btrplace.scheduler.choco.constraint.migration.CPrecedence}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.scheduler.choco.constraint.migration.CPrecedence
 */
public class CPrecedenceTest {

    @Test
    public void testOk() throws SchedulerException {
        
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
        // The destination node have twice the bandwidth of source nodes
        Switch swMain = net.newSwitch();
        net.connect(1000, swMain, srcNode1, srcNode2);
        net.connect(2000, swMain, dstNode);

        // Create and host 1 VM per source node
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        ma.addRunningVM(vm1, srcNode1);
        ma.addRunningVM(vm2, srcNode2);

        // Attach CPU and Mem resource views and assign nodes capacity and VMs consumption
        int mem_vm = 8, cpu_vm = 4, mem_src = 8, cpu_src = 4, mem_dst = 16, cpu_dst = 8;
        ShareableResource rcMem = new ShareableResource("mem", 0, 0), rcCPU = new ShareableResource("cpu", 0, 0);
        mo.attach(rcMem);
        mo.attach(rcCPU);
        // VMs
        rcMem.setConsumption(vm1, mem_vm).setConsumption(vm2, mem_vm);
        rcCPU.setConsumption(vm1, cpu_vm).setConsumption(vm2, cpu_vm);
        // Nodes
        rcMem.setCapacity(srcNode1, mem_src).setCapacity(srcNode2, mem_src).setCapacity(dstNode, mem_dst);
        rcCPU.setCapacity(srcNode1, cpu_src).setCapacity(srcNode2, cpu_src).setCapacity(dstNode, cpu_dst);

        // Set VM attributes 'memory used', 'hot dirty page size', 'hot dirty page duration' and 'cold dirty pages rate'
        int vm_mu = 6000, vm_mds = 46, vm_mdd = 2; double vm_cdr = 23.6;
        // vm1 is an 'idle' VM (with no special memory activity) but still consumes 6 GiB of memory
        mo.getAttributes().put(vm1, "memUsed", vm_mu);
        // vm2 consumes 6 GiB memory and has a memory intensive workload equivalent to "stress --vm 1000 --bytes 50K"
        mo.getAttributes().put(vm2, "memUsed", vm_mu); // VM with a workload
        mo.getAttributes().put(vm2, "hotDirtySize", vm_mds);
        mo.getAttributes().put(vm2, "hotDirtyDuration", vm_mdd);
        mo.getAttributes().put(vm2, "coldDirtyRate", vm_cdr);

        // Create constraints
        List<SatConstraint> cstrs = new ArrayList<>();

        // Placement constraints, we want to shutdown the source nodes to force the migration to destination nodes
        cstrs.add(new Offline(srcNode1));
        cstrs.add(new Offline(srcNode2));

        // MIGRATE VM2 BEFORE VM1
        Precedence prec = new Precedence(vm2, vm1);
        cstrs.add(prec);

        // Solve it using the Min Max Time To Repair Migration scheduling oriented objective
        ReconfigurationPlan p = new DefaultChocoScheduler().solve(mo, cstrs, new MinMTTRMig());
        Assert.assertNotNull(p);

        // Check if the precedence constraint is respected
        MigrateVM mig1 = null, mig2 = null ;
        for (Action a : p.getActions()) {
            if (a instanceof MigrateVM && ((MigrateVM) a).getVM().equals(vm1)) mig1 = (MigrateVM) a;
            if (a instanceof MigrateVM && ((MigrateVM) a).getVM().equals(vm2)) mig2 = (MigrateVM) a;
        }
        Assert.assertTrue(mig1.getStart() >= mig2.getEnd());

        // TODO: use methods on PrecedenceChecker to verify that the migrations are in the expected order ?
        Assert.assertTrue(prec.isSatisfied(p));
    }
}
