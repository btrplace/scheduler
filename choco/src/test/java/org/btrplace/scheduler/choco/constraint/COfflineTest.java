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

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.*;
import org.btrplace.model.constraint.Offline;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.MappingFiller;
import org.btrplace.scheduler.choco.duration.ConstantActionDuration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

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

        Mapping map = new MappingFiller(model.getMapping()).on(n1, n2).get();

        DefaultChocoScheduler cra = new DefaultChocoScheduler();
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantActionDuration(10));
        cra.setTimeLimit(-1);
        List x = Offline.newOffline(map.getAllNodes());
        ReconfigurationPlan plan = cra.solve(model, x);
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
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).get();

        Offline off = new Offline(n1);
        COffline coff = new COffline(off);

        Assert.assertTrue(coff.getMisPlacedVMs(mo).isEmpty());

        map.addRunningVM(vm1, n1);
        Assert.assertEquals(coff.getMisPlacedVMs(mo), map.getAllVMs());
    }

    @Test
    public void testSolvableProblem() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).run(n1, vm1).get();
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, Collections.<SatConstraint>singleton(new Offline(n1)));
        Assert.assertNotNull(plan);
        Model res = plan.getResult();
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n1));
    }

    @Test
    public void testUnsolvableProblem() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        Node n1 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1).run(n1, vm1).get();
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, Collections.<SatConstraint>singleton(new Offline(n1)));
        Assert.assertNull(plan);
    }
}
