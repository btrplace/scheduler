/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.view;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Fence;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.migration.MinMTTRMig;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.Switch;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link org.btrplace.scheduler.choco.view.CNetwork}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.scheduler.choco.view.CNetwork
 */
public class CNetworkTest {

    /**
     * Test the instantiation and the creation of the variables.
     *
     * @throws org.btrplace.scheduler.SchedulerException if an error occurs during the solving process (it should not)
     */
    @Test
    public void defaultTest() throws SchedulerException {

        // New default model
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        // Create and boot 1 source and 1 destination node
        Node srcNode = mo.newNode();
        Node dstNode = mo.newNode();
        ma.addOnlineNode(srcNode);
        ma.addOnlineNode(dstNode);

        // Attach a network view
        Network net = new Network();
        mo.attach(net);
        // Connect the nodes through a main non-blocking switch using 1 Gbit/s links
        Switch swMain = net.newSwitch();
        int bw = 1000;
        net.connect(bw, swMain, srcNode, dstNode);

        // Create and host 1 running VM on the source node
        VM vm = mo.newVM();
        ma.addRunningVM(vm, srcNode);

        // The VM consumes 6 GiB memory and has a memory intensive workload equivalent to "stress --vm 1000 --bytes 50K"
        int memUsed = 6000;
        int hotDirtySize = 46;
        int hotDirtyDuration = 2;
        double coldDirtyRate = 23.6;
        mo.getAttributes().put(vm, "memUsed", memUsed); // 6 GiB
        mo.getAttributes().put(vm, "hotDirtySize", hotDirtySize); // 46 MiB
        mo.getAttributes().put(vm, "hotDirtyDuration", hotDirtyDuration); // 2 sec.
        mo.getAttributes().put(vm, "coldDirtyRate", coldDirtyRate); // 23.6 MiB/sec.

        // Add constraints
        List<SatConstraint> cstrs = new ArrayList<>();
        // We force the migration to go on the destination node
        cstrs.add(new Fence(vm, Collections.singleton(dstNode)));

        // Try to solve using the custom Min MTTR objective for migration scheduling
        ReconfigurationPlan p = new DefaultChocoScheduler().solve(mo, cstrs, new MinMTTRMig());
        Assert.assertNotNull(p);

        // The switch is non-blocking
        Assert.assertEquals(swMain.getCapacity(), Integer.MAX_VALUE);

        // Check the migration path and bandwidth
        MigrateVM mig = (MigrateVM) p.getActions().stream().filter(s -> s instanceof MigrateVM).findFirst().get();
        Assert.assertTrue(net.getRouting().getPath(mig.getSourceNode(),
                mig.getDestinationNode()).containsAll(net.getLinks()));
        Assert.assertEquals(net.getRouting().getMaxBW(mig.getSourceNode(),
                mig.getDestinationNode()), bw);
        Assert.assertEquals(mig.getBandwidth(), bw);

        // Check the migration duration computation
        double bandwidth_octet = mig.getBandwidth() / 9;
        double durationMin;
        double durationColdPages;
        double durationHotPages;
        double durationTotal;
        durationMin = memUsed / bandwidth_octet;
        durationColdPages = ((hotDirtySize + ((durationMin - hotDirtyDuration) * coldDirtyRate)) /
                (bandwidth_octet - coldDirtyRate));
        durationHotPages = ((hotDirtySize / bandwidth_octet) * ((hotDirtySize / hotDirtyDuration) /
                (bandwidth_octet - (hotDirtySize / hotDirtyDuration))));
        durationTotal = durationMin + durationColdPages + durationHotPages;
        Assert.assertEquals((mig.getEnd() - mig.getStart()), (int) Math.round(durationTotal));
    }

    @Test
    public void testWithSwitchCapacity() {
        Model mo = new DefaultModel();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        VM v = mo.newVM();

        mo.getMapping().on(n1, n2).run(n1, v);
        ShareableResource mem = new ShareableResource("mem", 10000, 5000);
        Network net = new Network();
        mo.attach(net);
        mo.attach(mem);
        mo.getAttributes().put(v, "memUsed", 10000);

        Switch sw = net.newSwitch(1000);
        net.connect(2000, sw, n1, n2);
        ChocoScheduler s = new DefaultChocoScheduler();
        ReconfigurationPlan p = s.solve(mo, Collections.singletonList(new Fence(v, n2)));
        Assert.assertNotNull(p);

    }
}
