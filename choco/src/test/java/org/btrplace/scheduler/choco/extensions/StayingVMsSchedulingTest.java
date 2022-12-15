/*
 * Copyright  2022 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions;

import org.btrplace.model.*;
import org.btrplace.model.constraint.*;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Sets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class StayingVMsSchedulingTest {

    /**
     * This reproduces issue #431.
     */
    @Test
    public void test() {

        final Model mo = new DefaultModel();

        final VM vm0 = mo.newVM();
        final VM vm1 = mo.newVM();
        final VM vm2 = mo.newVM();
        final VM vm3 = mo.newVM();

        final Node n0 = mo.newNode();
        final Node n1 = mo.newNode();
        final Node n2 = mo.newNode();
        final ShareableResource mem = new ShareableResource("mem", 8, 4);
        final ShareableResource cpu = new ShareableResource("cpu", 4, 2);
        mo.attach(mem);
        mo.attach(cpu);

        // N0(cpu=4, mem=8): VM0(2->3, 4) VM1(2->3, 4)
        // N1(cpu=4, mem=8):
        // N2(cpu:4, mem=8): VM2(2, 4) VM3(2, 4)
        // No VMs go in N0, VMs on N2 must stay.
        mo.getMapping().on(n0, n1, n2).run(n0, vm0, vm1).run(n2, vm2, vm3);
        final List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Root.newRoots(Arrays.asList(vm2, vm3)));
        final List<VM> hotspots = Arrays.asList(vm0, vm1);
        cstrs.addAll(Preserve.newPreserve(hotspots, "cpu", 3));
        cstrs.addAll(Ban.newBan(hotspots, Arrays.asList(n2)));

        final Instance ii = new Instance(mo, cstrs, new MinMigrations());
        final ChocoScheduler sched = new DefaultChocoScheduler();
        final ReconfigurationPlan plan = sched.solve(ii);
        Assert.assertNotNull(plan);
        Assert.assertEquals(2, plan.getActions().size());
        final Set<VM> impactedVms = Sets.newHashSet(vm0, vm1);
        for (final Action aa : plan.getActions()) {
            if (aa instanceof Allocate) {
                final Allocate al = (Allocate) aa;
                Assert.assertTrue(impactedVms.remove(al.getVM()));
                Assert.assertEquals(3, al.getAmount());
                Assert.assertEquals("cpu", al.getResourceId());
            } else if (aa instanceof MigrateVM) {
                final MigrateVM mig = (MigrateVM) aa;
                Assert.assertTrue(impactedVms.remove(mig.getVM()));
                Assert.assertEquals(mig.getDestinationNode(), n1);
            } else {
                Assert.fail("Unexpected action: " + aa);
            }
        }
    }
}