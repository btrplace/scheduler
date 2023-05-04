/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.examples.migration;

import org.btrplace.examples.Example;
import org.btrplace.model.*;
import org.btrplace.model.constraint.Offline;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.Switch;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vincent Kherbache
 */
@SuppressWarnings("squid:S106")
public class NetworkAndMigrations implements Example {

    private static final String MEM_USED_ATTR = "memUsed";

    @Override
    public void run() {

        // New default model
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        // Create and boot 2 source nodes and 1 destination node
        Node srcNode1 = mo.newNode();
        Node srcNode2 = mo.newNode();
        Node dstNode = mo.newNode();
        ma.addOnlineNode(srcNode1);
        ma.addOnlineNode(srcNode2);
        ma.addOnlineNode(dstNode);

        // Create 4 VMs and host 2 VMs on each source node
        VM vm0 = mo.newVM();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        ma.addRunningVM(vm0, srcNode1);
        ma.addRunningVM(vm1, srcNode1);
        ma.addRunningVM(vm2, srcNode2);
        ma.addRunningVM(vm3, srcNode2);

        // Set VM attributes 'memory used', 'hot dirty page size', 'hot dirty page duration' and 'cold dirty pages rate'
        // vm0 and vm3 are 'idle' VMs (with no special memory activity) but they still consume some memory
        mo.getAttributes().put(vm0, MEM_USED_ATTR, 2000); // 2 GiB
        mo.getAttributes().put(vm3, MEM_USED_ATTR, 2200); // 2.2 GiB
        // vm1 and vm2 consume memory and have a memory intensive workload equivalent to "stress --vm 1000 --bytes 50K"
        mo.getAttributes().put(vm1, MEM_USED_ATTR, 8000); // 8 GiB
        mo.getAttributes().put(vm1, "hotDirtySize", 56);
        mo.getAttributes().put(vm1, "hotDirtyDuration", 2);
        mo.getAttributes().put(vm1, "coldDirtyRate", 22.6);
        mo.getAttributes().put(vm2, MEM_USED_ATTR, 7500); // 7.5 GiB
        mo.getAttributes().put(vm2, "hotDirtySize", 56);
        mo.getAttributes().put(vm2, "hotDirtyDuration", 2);
        mo.getAttributes().put(vm2, "coldDirtyRate", 22.6);

        // Add placement constraints: we want to shutdown the source nodes to force VMs migration to destination nodes
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Offline(srcNode1));
        cstrs.add(new Offline(srcNode2));

        // Try to solve as is and show the computed plan
        ReconfigurationPlan p = new DefaultChocoScheduler().solve(mo, cstrs);
        System.out.println(p);


        // Set a default network view and try to solve again
        // connect nodes to a non-blocking switch using 1 Gbit/s links
        Network net = Network.createDefaultNetwork(mo);
        p = new DefaultChocoScheduler().solve(mo, cstrs);
        System.out.println(p);

        // Create and attach a custom network view and try to solve again
        mo.detach(net);
        net = new Network();
        // Connect the nodes through a main non-blocking switch
        // The source nodes are connected with 1Gbit/sec. links while the destination node has 10Gbit/sec. link.
        Switch swMain = net.newSwitch();
        net.connect(1000, swMain, srcNode1, srcNode2);
        net.connect(10000, swMain, dstNode);
        mo.attach(net);
        p = new DefaultChocoScheduler().solve(mo, cstrs);
        System.out.println(p);
        System.out.flush();
    }
}
