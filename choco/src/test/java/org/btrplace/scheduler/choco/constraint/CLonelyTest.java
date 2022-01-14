/*
 * Copyright  2022 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.*;
import org.btrplace.model.constraint.Fence;
import org.btrplace.model.constraint.Lonely;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link CLonely}.
 *
 * @author Fabien Hermenier
 */
public class CLonelyTest {

    @Test
    public void testFeasibleDiscrete() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        mo.getMapping().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4, vm5);

        Set<VM> mine = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        ChocoScheduler cra = new DefaultChocoScheduler();
        Lonely l = new Lonely(mine);
        l.setContinuous(false);
        cra.setVerbosity(1);
        ReconfigurationPlan plan = cra.solve(mo, Collections.singleton(l));
        Assert.assertNotNull(plan);
    }

    /**
     * vm3 needs to go to n2 . vm1, vm2, vm3 must be lonely. n2 is occupied by "other" VMs, so
     * they have to go away before receiving vm3
     *
     * @throws org.btrplace.scheduler.SchedulerException
     */
    @Test
    public void testFeasibleContinuous() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        mo.getMapping().on(n1, n2, n3)
                .run(n1, vm1, vm2, vm3)
                .run(n2, vm4, vm5);
        Set<VM> mine = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        ChocoScheduler cra = new DefaultChocoScheduler();
        Lonely l = new Lonely(mine);
        l.setContinuous(true);
        Set<SatConstraint> cstrs = new HashSet<>();
        cstrs.add(l);
        cstrs.add(new Fence(vm3, Collections.singleton(n2)));
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
    }

    @Test
    public void testGetMisplaced() {

        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = mo.getMapping().on(n1, n2, n3)
                .run(n1, vm1, vm2, vm3)
                .run(n2, vm4, vm5);
        Set<VM> mine = new HashSet<>(Arrays.asList(vm1, vm2, vm3));


        CLonely c = new CLonely(new Lonely(mine));
        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        Assert.assertTrue(c.getMisPlacedVMs(i).isEmpty());
        map.addRunningVM(vm2, n2);
        Assert.assertEquals(c.getMisPlacedVMs(i), map.getRunningVMs(n2));
    }

    @Test
    public void testWithNonPersistingVMs() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        mo.getMapping().on(n1, n2, n3)
                .ready(vm1, vm2);
        Set<VM> mine = new HashSet<>(Arrays.asList(vm1, vm2));
        Lonely l = new Lonely(mine);
        Instance i = new Instance(mo, Collections.singleton(l), new MinMTTR());
        ChocoScheduler cra = new DefaultChocoScheduler();
        Assert.assertNotNull(cra.solve(i));
        Assert.assertEquals(cra.getStatistics().lastSolution().getSize(), 0);
    }
}
