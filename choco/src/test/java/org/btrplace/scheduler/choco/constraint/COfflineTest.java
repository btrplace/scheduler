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

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.*;
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
