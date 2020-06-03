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
import org.btrplace.model.constraint.Fence;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.SplitAmong;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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

        Mapping map = mo.getMapping().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7);

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Collection<VM> vg1 = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        Collection<VM> vg2 = new HashSet<>(Arrays.asList(vm4, vm5, vm6));
        Collection<VM> vg3 = new HashSet<>(Collections.singletonList(vm7));

        Collection<Node> pg1 = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> pg2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Node> pg3 = new HashSet<>(Collections.singletonList(n5));
        Collection<Collection<VM>> vgs = new HashSet<>(Arrays.asList(vg1, vg2, vg3));
        Collection<Collection<Node>> pgs = new HashSet<>(Arrays.asList(pg1, pg2, pg3));

        SplitAmong s = new SplitAmong(vgs, pgs);
        CSplitAmong cs = new CSplitAmong(s);
        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        Assert.assertTrue(cs.getMisPlacedVMs(i).isEmpty());


        map.remove(vm7);
        map.addRunningVM(vm6, n5);
        //vg2 is on 2 group of nodes, the whole group is mis-placed

        Assert.assertEquals(cs.getMisPlacedVMs(i), vg2);


        map.addRunningVM(vm7, n5);
        //vg1 and vg2 overlap on n1. The two groups are mis-placed
        map.addRunningVM(vm6, n2);

        Assert.assertTrue(cs.getMisPlacedVMs(i).containsAll(vg1));
        Assert.assertTrue(cs.getMisPlacedVMs(i).containsAll(vg2));
        Assert.assertEquals(cs.getMisPlacedVMs(i).size(), vg1.size() + vg2.size());
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

        Mapping map = mo.getMapping().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7);

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Collection<VM> vg1 = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        Collection<VM> vg2 = new HashSet<>(Arrays.asList(vm4, vm5, vm6));
        Collection<VM> vg3 = new HashSet<>(Collections.singletonList(vm7));

        Collection<Node> pg1 = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> pg2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Node> pg3 = new HashSet<>(Collections.singletonList(n5));
        Collection<Collection<VM>> vgs = new HashSet<>(Arrays.asList(vg1, vg2, vg3));
        Collection<Collection<Node>> pgs = new HashSet<>(Arrays.asList(pg1, pg2, pg3));

        SplitAmong s = new SplitAmong(vgs, pgs);
        s.setContinuous(false);

        //vg1 and vg2 overlap on n2. The two groups are mis-placed
        map.addRunningVM(vm6, n2);

        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan p = cra.solve(mo, Collections.singleton(s));
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

        Mapping map = mo.getMapping().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7);

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Collection<VM> vg1 = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        Collection<VM> vg2 = new HashSet<>(Arrays.asList(vm4, vm5, vm6));
        Collection<VM> vg3 = new HashSet<>(Collections.singletonList(vm7));

        Collection<Node> pg1 = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> pg2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Node> pg3 = new HashSet<>(Collections.singletonList(n5));
        Collection<Collection<VM>> vgs = new HashSet<>(Arrays.asList(vg1, vg2, vg3));
        Collection<Collection<Node>> pgs = new HashSet<>(Arrays.asList(pg1, pg2, pg3));

        SplitAmong s = new SplitAmong(vgs, pgs);
        s.setContinuous(true);

        //vg1 and vg2 overlap on n2.
        map.addRunningVM(vm6, n2);

        ChocoScheduler cra = new DefaultChocoScheduler();
        Assert.assertNull(cra.solve(mo, Collections.singleton(s)));
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

        Mapping map = mo.getMapping().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7);

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Collection<VM> vg1 = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        Collection<VM> vg2 = new HashSet<>(Arrays.asList(vm4, vm5, vm6));

        Collection<Node> pg1 = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> pg2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Node> pg3 = new HashSet<>(Collections.singletonList(n5));
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

        Mapping map = mo.getMapping().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7);

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Collection<VM> vg1 = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        Collection<VM> vg2 = new HashSet<>(Arrays.asList(vm4, vm5, vm6));

        Collection<Node> pg1 = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> pg2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Node> pg3 = new HashSet<>(Collections.singletonList(n5));
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
