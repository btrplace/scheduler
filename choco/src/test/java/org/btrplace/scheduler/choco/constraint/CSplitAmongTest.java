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
import org.btrplace.model.constraint.SplitAmong;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CSplitAmong}.
 *
 * @author Fabien Hermenier
 */
public class CSplitAmongTest {

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
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7).get();

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Collection<VM> vg1 = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        Collection<VM> vg2 = new HashSet<>(Arrays.asList(vm4, vm5, vm6));
        Collection<VM> vg3 = new HashSet<>(Arrays.asList(vm7));

        Collection<Node> pg1 = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> pg2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Node> pg3 = new HashSet<>(Arrays.asList(n5));
        Collection<Collection<VM>> vgs = new HashSet<>(Arrays.asList(vg1, vg2, vg3));
        Collection<Collection<Node>> pgs = new HashSet<>(Arrays.asList(pg1, pg2, pg3));

        SplitAmong s = new SplitAmong(vgs, pgs);
        CSplitAmong cs = new CSplitAmong(s);

        Assert.assertTrue(cs.getMisPlacedVMs(mo).isEmpty());


        map.remove(vm7);
        map.addRunningVM(vm6, n5);
        //vg2 is on 2 group of nodes, the whole group is mis-placed

        Assert.assertEquals(cs.getMisPlacedVMs(mo), vg2);


        map.addRunningVM(vm7, n5);
        //vg1 and vg2 overlap on n1. The two groups are mis-placed
        map.addRunningVM(vm6, n2);

        Assert.assertTrue(cs.getMisPlacedVMs(mo).containsAll(vg1));
        Assert.assertTrue(cs.getMisPlacedVMs(mo).containsAll(vg2));
        Assert.assertEquals(cs.getMisPlacedVMs(mo).size(), vg1.size() + vg2.size());
    }

    @Test
    public void testDiscrete() throws SchedulerException {
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
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7).get();

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Collection<VM> vg1 = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        Collection<VM> vg2 = new HashSet<>(Arrays.asList(vm4, vm5, vm6));
        Collection<VM> vg3 = new HashSet<>(Arrays.asList(vm7));

        Collection<Node> pg1 = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> pg2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Node> pg3 = new HashSet<>(Arrays.asList(n5));
        Collection<Collection<VM>> vgs = new HashSet<>(Arrays.asList(vg1, vg2, vg3));
        Collection<Collection<Node>> pgs = new HashSet<>(Arrays.asList(pg1, pg2, pg3));

        SplitAmong s = new SplitAmong(vgs, pgs);
        s.setContinuous(false);

        //vg1 and vg2 overlap on n2. The two groups are mis-placed
        map.addRunningVM(vm6, n2);

        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>singleton(s));
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getSize() > 0);
    }

    @Test
    public void testContinuousWithAllDiffViolated() throws SchedulerException {
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
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7).get();

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Collection<VM> vg1 = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        Collection<VM> vg2 = new HashSet<>(Arrays.asList(vm4, vm5, vm6));
        Collection<VM> vg3 = new HashSet<>(Arrays.asList(vm7));

        Collection<Node> pg1 = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> pg2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Node> pg3 = new HashSet<>(Arrays.asList(n5));
        Collection<Collection<VM>> vgs = new HashSet<>(Arrays.asList(vg1, vg2, vg3));
        Collection<Collection<Node>> pgs = new HashSet<>(Arrays.asList(pg1, pg2, pg3));

        SplitAmong s = new SplitAmong(vgs, pgs);
        s.setContinuous(true);

        //vg1 and vg2 overlap on n2.
        map.addRunningVM(vm6, n2);

        ChocoScheduler cra = new DefaultChocoScheduler();
        Assert.assertNull(cra.solve(mo, Collections.<SatConstraint>singleton(s)));
    }

    @Test
    public void testContinuousWithGroupChange() throws SchedulerException {
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
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7).get();

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Collection<VM> vg1 = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        Collection<VM> vg2 = new HashSet<>(Arrays.asList(vm4, vm5, vm6));

        Collection<Node> pg1 = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> pg2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Node> pg3 = new HashSet<>(Arrays.asList(n5));
        Collection<Collection<VM>> vgs = new HashSet<>(Arrays.asList(vg1, vg2));
        Collection<Collection<Node>> pgs = new HashSet<>(Arrays.asList(pg1, pg2, pg3));

        List<SatConstraint> cstrs = new ArrayList<>();
        SplitAmong s = new SplitAmong(vgs, pgs);
        s.setContinuous(true);

        //Move group of VMs 1 to the group of nodes 2. Cannot work as
        //the among part of the constraint will be violated


        cstrs.add(s);
        for (VM v : vg1) {
            cstrs.add(new Fence(v, pg2));
        }

        ChocoScheduler cra = new DefaultChocoScheduler();
        Assert.assertNull(cra.solve(mo, cstrs));
    }

    @Test
    public void testDiscreteWithGroupChange() throws SchedulerException {
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
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7).get();

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Collection<VM> vg1 = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        Collection<VM> vg2 = new HashSet<>(Arrays.asList(vm4, vm5, vm6));

        Collection<Node> pg1 = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> pg2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Node> pg3 = new HashSet<>(Arrays.asList(n5));
        Collection<Collection<VM>> vgs = new HashSet<>(Arrays.asList(vg1, vg2));
        Collection<Collection<Node>> pgs = new HashSet<>(Arrays.asList(pg1, pg2, pg3));

        List<SatConstraint> cstrs = new ArrayList<>();
        SplitAmong s = new SplitAmong(vgs, pgs);
        s.setContinuous(false);

        //Move group of VMs 1 to the group of nodes 2. This is allowed
        //group of VMs 2 will move to another group of node so at the end, the constraint should be satisfied

        cstrs.add(s);
        for (VM v : vg1) {
            cstrs.add(new Fence(v, pg2));
        }

        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
    }

}
