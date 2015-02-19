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
import org.btrplace.model.constraint.*;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CRunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CResourceCapacityTest {

    @Test
    public void testWithSatisfiedConstraint() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n3, vm3, vm4)
                .sleep(n2, vm5).get();

        ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 3);
        rc.setConsumption(vm4, 1);
        rc.setConsumption(vm5, 5);

        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<>();
        ResourceCapacity x = new ResourceCapacity(map.getAllNodes(), "cpu", 10);
        x.setContinuous(false);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        Assert.assertEquals(plan.getSize(), 0);
    }

    @Test
    public void testDiscreteSatisfaction() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4, vm5).get();

        Set<Node> on = new HashSet<>(Arrays.asList(n1, n2));

        org.btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 3);
        rc.setConsumption(vm4, 1);
        rc.setConsumption(vm5, 1);

        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<>();
        ResourceCapacity x = new ResourceCapacity(on, "cpu", 9);
        x.setContinuous(false);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        Assert.assertTrue(plan.getSize() > 0);
    }

    @Test
    public void testFeasibleContinuousResolution() throws SchedulerException {

        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4)
                .ready(vm5).get();
        Set<Node> on = new HashSet<>(Arrays.asList(n1, n2));

        org.btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 3);
        rc.setConsumption(vm4, 1);
        rc.setConsumption(vm5, 5);
        mo.attach(rc);

        List<SatConstraint> l = new ArrayList<>();
        l.add(new Running(vm5));
        l.add(new Fence(vm5, Collections.singleton(n1)));
        ResourceCapacity x = new ResourceCapacity(on, "cpu", 10);
        x.setContinuous(true);
        l.add(x);
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        //System.out.println(plan);
        Assert.assertTrue(plan.getSize() > 0);
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
        Mapping m = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2, vm3).run(n2, vm4).ready(vm5).get();

        ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 3);
        rc.setConsumption(vm4, 1);
        rc.setConsumption(vm5, 5);
        mo.attach(rc);
        ResourceCapacity c = new ResourceCapacity(m.getAllNodes(), "cpu", 10);
        CResourceCapacity cc = new CResourceCapacity(c);

        Assert.assertTrue(cc.getMisPlacedVMs(mo).isEmpty());
        m.addRunningVM(vm5, n3);
        Assert.assertEquals(cc.getMisPlacedVMs(mo), m.getAllVMs());
    }

    @Test
    public void testSingleGetMisplaced() {
        Model mo = new DefaultModel();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n2).run(n2, vm2, vm3).get();

        org.btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 1);

        mo.attach(rc);

        ResourceCapacity s = new ResourceCapacity(n2, "cpu", 4);
        CResourceCapacity cs = new CResourceCapacity(s);
        Assert.assertTrue(cs.getMisPlacedVMs(mo).isEmpty());
        rc.setConsumption(vm3, 2);
        Assert.assertEquals(cs.getMisPlacedVMs(mo), map.getRunningVMs(n2));
    }

    @Test
    public void testDiscreteSolvable() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).run(n1, vm1, vm2).get();

        ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 1);
        rc.setConsumption(vm3, 1);

        mo.attach(rc);

        ResourceCapacity s = new ResourceCapacity(n1, "cpu", 4);

        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan p = cra.solve(mo, Arrays.asList(s, new Preserve(vm2, "cpu", 3)));
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 1);
        //System.out.println(p);
    }

    @Test
    public void testDiscreteUnsolvable() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).on(n1).run(n1, vm1, vm2).get();

        org.btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 2);

        mo.attach(rc);

        ResourceCapacity s = new ResourceCapacity(n1, "cpu", 3);

        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>singleton(s));
        Assert.assertNull(p);
    }

    @Test
    public void testSingleContinuousSolvable() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).run(n1, vm1, vm2).ready(vm4).get();
        ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 1);
        rc.setConsumption(vm3, 1);
        rc.setConsumption(vm4, 3);

        mo.attach(rc);

        List<SatConstraint> cstrs = new ArrayList<>();

        ResourceCapacity s = new ResourceCapacity(n1, "cpu", 4);
        s.setContinuous(true);

        cstrs.add(s);
        cstrs.add(new Fence(vm4, Collections.singleton(n1)));
        cstrs.add(new Running(vm4));
        cstrs.addAll(Overbook.newOverbooks(map.getAllNodes(), "cpu", 1));
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(p.getSize(), 2);

    }
}
