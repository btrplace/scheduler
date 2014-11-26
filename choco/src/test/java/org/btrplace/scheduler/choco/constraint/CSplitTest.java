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
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.Split;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CSplit}.
 *
 * @author Fabien Hermenier
 */
public class CSplitTest {

    @Test
    public void testGetMisplaced() {

        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        VM vm7 = mo.newVM();
        VM vm8 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();
        Node n5 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm2)
                .run(n2, vm3)
                .run(n3, vm4, vm5)
                .run(n4, vm6)
                .run(n5, vm7, vm8).get();

        Collection<VM> g1 = Arrays.asList(vm1, vm2);
        Collection<VM> g2 = new HashSet<>(Arrays.asList(vm3, vm4, vm5));
        Collection<VM> g3 = new HashSet<>(Arrays.asList(vm6, vm7));
        Collection<Collection<VM>> grps = Arrays.asList(g1, g2, g3);
        Split s = new Split(grps);
        CSplit cs = new CSplit(s);

        Assert.assertTrue(cs.getMisPlacedVMs(mo).isEmpty());

        map.addRunningVM(vm5, n1);
        Set<VM> bad = cs.getMisPlacedVMs(mo);
        Assert.assertEquals(bad.size(), 3);

        Assert.assertTrue(bad.contains(vm1) && bad.contains(vm2) && bad.contains(vm5));
        map.addRunningVM(vm6, n3);
        bad = cs.getMisPlacedVMs(mo);
        Assert.assertTrue(bad.contains(vm4) && bad.contains(vm5) && bad.contains(vm6));

    }

    @Test
    public void testSimpleDiscrete() throws SchedulerException {

        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        VM vm7 = mo.newVM();
        VM vm8 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();
        Node n5 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm2, vm3/* violation*/)
                .run(n3, vm4, vm5, vm6/*violation*/)
                .run(n5, vm7, vm8).get();

        Collection<VM> g1 = Arrays.asList(vm1, vm2);
        Collection<VM> g2 = Arrays.asList(vm3, vm4, vm5);
        Collection<VM> g3 = Arrays.asList(vm6, vm7);

        Collection<Collection<VM>> grps = Arrays.asList(g1, g2, g3);
        Split s = new Split(grps);

        ChocoScheduler cra = new DefaultChocoScheduler();
        //cra.labelVariables(true);
        //cra.setVerbosity(3);
        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>singleton(s));
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getSize() > 0);
        //System.out.println(p);
    }

    @Test
    public void testContinuous() throws SchedulerException {

        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        VM vm7 = mo.newVM();
        VM vm8 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();
        Node n5 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm2)
                .run(n3, vm3, vm4, vm5)
                .run(n5, vm6, vm7, vm8).get();

        Collection<VM> g1 = Arrays.asList(vm1, vm2);
        Collection<VM> g2 = Arrays.asList(vm3, vm4, vm5);
        Collection<VM> g3 = Arrays.asList(vm6, vm7);
        Collection<Collection<VM>> grps = Arrays.asList(g1, g2, g3);
        Split s = new Split(grps);

        s.setContinuous(true);

        ChocoScheduler cra = new DefaultChocoScheduler();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(s);
        //What is running on n1 goes to n3, so VMs vm3, vm4, vm5 which does not belong to vm1, vm2 must
        //go away before the other arrive.
        for (VM v : map.getRunningVMs(n1)) {
            cstrs.add(new Fence(v, Collections.singleton(n3)));
        }
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getSize() > 0);
    }
}
