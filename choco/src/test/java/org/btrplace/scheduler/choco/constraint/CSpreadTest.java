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
import org.btrplace.model.constraint.Fence;
import org.btrplace.model.constraint.Online;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.Spread;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CSpread}.
 *
 * @author Fabien Hermenier
 */
public class CSpreadTest {

    @Test
    public void testDiscrete() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1).run(n2, vm2).get();

        List<SatConstraint> cstr = new ArrayList<>();
        ChocoScheduler cra = new DefaultChocoScheduler();
        Spread s = new Spread(mo.getMapping().getAllVMs());
        s.setContinuous(false);
        cstr.add(s);
        cstr.addAll(Online.newOnline(mo.getMapping().getAllNodes()));
        cstr.add(new Fence(vm1, Collections.singleton(n2)));
        ReconfigurationPlan p = cra.solve(mo, cstr);
        Assert.assertNotNull(p);
        System.err.println(p);
        Mapping res = p.getResult().getMapping();
        Assert.assertEquals(2, p.getSize());
        Assert.assertNotSame(res.getVMLocation(vm1), res.getVMLocation(vm2));
    }

    @Test
    public void testContinuous() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1).run(n2, vm2).get();

        List<SatConstraint> cstr = new ArrayList<>();
        ChocoScheduler cra = new DefaultChocoScheduler();
        cstr.add(new Spread(mo.getMapping().getAllVMs(), true));
        cstr.addAll(Online.newOnline(mo.getMapping().getAllNodes()));
        cstr.add(new Fence(vm1, Collections.singleton(n2)));
        ReconfigurationPlan p = cra.solve(mo, cstr);
        Assert.assertNotNull(p);
        System.err.println(p);
        Mapping res = p.getResult().getMapping();
        Assert.assertEquals(2, p.getSize());
        Assert.assertNotSame(res.getVMLocation(vm1), res.getVMLocation(vm2));
    }

    @Test
    public void testGetMisplaced() {

        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2)
                .run(n1, vm1, vm3)
                .run(n2, vm2).get();
        Set<VM> vms = new HashSet<>(Arrays.asList(vm1, vm2));
        Spread s = new Spread(vms);
        CSpread cs = new CSpread(s);

        Assert.assertTrue(cs.getMisPlacedVMs(mo).isEmpty());
        vms.add(vm3);
        Assert.assertEquals(map.getRunningVMs(n1), cs.getMisPlacedVMs(mo));
    }

    /**
     * 2 VMs are already hosted on a same node, check
     * if separation is working in continuous mode
     */
    @Test
    public void testSeparateWithContinuous() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).run(n1, vm1, vm2).get();

        List<SatConstraint> cstr = new ArrayList<>();
        ChocoScheduler cra = new DefaultChocoScheduler();
        Spread s = new Spread(mo.getMapping().getAllVMs());
        s.setContinuous(true);
        cstr.add(s);
        cstr.addAll(Online.newOnline(mo.getMapping().getAllNodes()));
        cstr.add(new Fence(vm1, Collections.singleton(n2)));
        ReconfigurationPlan p = cra.solve(mo, cstr);
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 1);
        Mapping res = p.getResult().getMapping();
        Assert.assertNotSame(res.getVMLocation(vm1), res.getVMLocation(vm2));
    }
}
