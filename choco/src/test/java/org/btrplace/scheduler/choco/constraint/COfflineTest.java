/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Offline;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.duration.ConstantActionDuration;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * Unit tests for {@link COffline}.
 *
 * @author Fabien Hermenier
 */
public class COfflineTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        Node n1 = mo.newNode();
        Offline b = new Offline(n1);
        COffline c = new COffline(b);
        Assert.assertEquals(c.toString(), b.toString());
    }

    /**
     * Simple test, no VMs.
     */
    @Test
    public void simpleTest() throws SchedulerException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();

        Mapping map = model.getMapping().on(n1, n2);

        DefaultChocoScheduler cra = new DefaultChocoScheduler();
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantActionDuration<>(10));
        cra.setTimeLimit(-1);
        ReconfigurationPlan plan = cra.solve(model, Offline.newOffline(map.getAllNodes()));
        Assert.assertNotNull(plan);
        Assert.assertEquals(plan.getSize(), 2);
        Assert.assertEquals(plan.getDuration(), 10);
        Model res = plan.getResult();
        Assert.assertEquals(res.getMapping().getOfflineNodes().size(), 2);
    }

    @Test
    public void testGetMisplacedAndSatisfied() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = mo.getMapping().on(n1, n2);

        Offline off = new Offline(n1);
        COffline coff = new COffline(off);

        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        Assert.assertTrue(coff.getMisPlacedVMs(i).isEmpty());

        map.addRunningVM(vm1, n1);
        Assert.assertEquals(coff.getMisPlacedVMs(i), map.getAllVMs());
    }

    @Test
    public void testSolvableProblem() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        mo.getMapping().on(n1, n2).run(n1, vm1);
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, Collections.singleton(new Offline(n1)));
        Assert.assertNotNull(plan);
        Model res = plan.getResult();
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n1));
    }

    @Test
    public void testUnsolvableProblem() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        Node n1 = mo.newNode();
        mo.getMapping().on(n1).run(n1, vm1);
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, Collections.singleton(new Offline(n1)));
        Assert.assertNull(plan);
        SolvingStatistics stats = cra.getStatistics();
        Assert.assertTrue(stats.completed());
    }
}
