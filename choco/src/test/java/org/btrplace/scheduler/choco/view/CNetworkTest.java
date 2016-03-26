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

package org.btrplace.scheduler.choco.view;

import org.btrplace.model.*;
import org.btrplace.model.constraint.Fence;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.migration.MinMTTRMig;
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
        Node srcNode = mo.newNode(), dstNode = mo.newNode();
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
        int memUsed = 6000, hotDirtySize = 46, hotDirtyDuration = 2;
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
        Assert.assertEquals(swMain.getCapacity(), -1);
        
        // Check the migration path and bandwidth
        MigrateVM mig = null;
        for (Action a : p.getActions()) { if (a instanceof MigrateVM) {
            mig = (MigrateVM) a;
            Assert.assertTrue(net.getRouting().getPath(((MigrateVM) a).getSourceNode(),
                    ((MigrateVM) a).getDestinationNode()).containsAll(net.getLinks()));
            Assert.assertEquals(net.getRouting().getMaxBW(((MigrateVM) a).getSourceNode(),
                    ((MigrateVM) a).getDestinationNode()), bw);
            Assert.assertEquals(((MigrateVM) a).getBandwidth(), bw);
            break;
        }}

        // Check the migration duration computation
        double bandwidth_octet = mig.getBandwidth()/9, durationMin, durationColdPages, durationHotPages, durationTotal;
        durationMin = memUsed / bandwidth_octet;
        durationColdPages = ((hotDirtySize + ((durationMin - hotDirtyDuration) * coldDirtyRate)) /
                (bandwidth_octet - coldDirtyRate));
        durationHotPages = ((hotDirtySize / bandwidth_octet) * ((hotDirtySize / hotDirtyDuration) /
                (bandwidth_octet - (hotDirtySize / hotDirtyDuration))));
        durationTotal = durationMin + durationColdPages + durationHotPages;
        Assert.assertEquals((mig.getEnd() - mig.getStart()), (int) Math.round(durationTotal));
    }
}
