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
import org.btrplace.model.constraint.Split;
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
import java.util.Set;

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

        Mapping map = mo.getMapping().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm2)
                .run(n2, vm3)
                .run(n3, vm4, vm5)
                .run(n4, vm6)
                .run(n5, vm7, vm8);

        Collection<VM> g1 = Arrays.asList(vm1, vm2);
        Collection<VM> g2 = new HashSet<>(Arrays.asList(vm3, vm4, vm5));
        Collection<VM> g3 = new HashSet<>(Arrays.asList(vm6, vm7));
        Collection<Collection<VM>> grps = Arrays.asList(g1, g2, g3);
        Split s = new Split(grps);
        CSplit cs = new CSplit(s);

        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        Assert.assertTrue(cs.getMisPlacedVMs(i).isEmpty());

        map.addRunningVM(vm5, n1);
        Set<VM> bad = cs.getMisPlacedVMs(i);
        Assert.assertEquals(bad.size(), 3);

        Assert.assertTrue(bad.contains(vm1) && bad.contains(vm2) && bad.contains(vm5));
        map.addRunningVM(vm6, n3);
        bad = cs.getMisPlacedVMs(i);
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

        mo.getMapping().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm2, vm3/* violation*/)
                .run(n3, vm4, vm5, vm6/*violation*/)
                .run(n5, vm7, vm8);

        Collection<VM> g1 = Arrays.asList(vm1, vm2);
        Collection<VM> g2 = Arrays.asList(vm3, vm4, vm5);
        Collection<VM> g3 = Arrays.asList(vm6, vm7);

        Collection<Collection<VM>> grps = Arrays.asList(g1, g2, g3);
        Split s = new Split(grps);

        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan p = cra.solve(mo, Collections.singleton(s));
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getSize() > 0);
    }

    //@Test
    @Test(enabled = false)
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
        Mapping map = mo.getMapping().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm2)
                .run(n3, vm3, vm4, vm5)
                .run(n5, vm6, vm7, vm8);

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
        //cra.setTimeLimit(2);
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getSize() > 0);
    }
}
