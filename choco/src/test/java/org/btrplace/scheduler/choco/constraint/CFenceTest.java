/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.*;
import org.btrplace.model.constraint.*;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link org.btrplace.model.constraint.Fence}.
 *
 * @author Fabien Hermenier
 */
public class CFenceTest {

    /**
     * Test getMisPlaced() in various situations.
     */
    @Test
    public void testGetMisPlaced() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();
        Node n5 = mo.newNode();
        mo.getMapping().on(n1, n2, n3, n4)
                .off(n5)
                .run(n1, vm1, vm2)
                .run(n2, vm3)
                .run(n3, vm4)
                .sleep(n4, vm5);

        Set<Node> ns = new HashSet<>(Arrays.asList(n1, n2));
        CFence c = new CFence(new Fence(vm1, ns));
        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        Assert.assertTrue(c.getMisPlacedVMs(i).isEmpty());
        ns.add(mo.newNode());
        Assert.assertTrue(c.getMisPlacedVMs(i).isEmpty());
        Assert.assertTrue(c.getMisPlacedVMs(i).isEmpty());
        Set<VM> bad = new CFence(new Fence(vm4, ns)).getMisPlacedVMs(i);
        Assert.assertEquals(bad.size(), 1);
        Assert.assertTrue(bad.contains(vm4));
    }

    @Test
    public void testBasic() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Mapping map = mo.getMapping().on(n1, n2, n3)
                .run(n1, vm1, vm4)
                .run(n2, vm2)
                .run(n3, vm3);

        Set<Node> on = new HashSet<>(Arrays.asList(n1, n3));
        Fence f = new Fence(vm2, on);
        ChocoScheduler cra = new DefaultChocoScheduler();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(f);
        cstrs.addAll(Online.newOnline(map.getAllNodes()));
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getSize() > 0);
        Assert.assertTrue(p.iterator().next() instanceof MigrateVM);
        cstrs.add(new Ready(vm2));

        p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 1); //Just the suspend of vm2
    }
}
