/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Online;
import org.btrplace.model.constraint.Preserve;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


/**
 * Unit tests for {@link CPreserve}.
 *
 * @author Fabien Hermenier
 */
public class CPreserveTest {

    /*@Test
    public void testGetMisplaced() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        Mapping map = mo.getMapping().on(n1, n2).run(n1, vm1, vm2).run(n2, vm3).get();
        ShareableResource rc = new ShareableResource("cpu", 7, 7);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 5);

        Preserve p = new Preserve(vm1, "cpu", 5);

        mo.attach(rc);
        //Assert.assertEquals(SatConstraint.Sat.UNSATISFIED, p.isSatisfied(mo));

        CPreserve cp = new CPreserve(p);
        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        Set<VM> bads = cp.getMisPlacedVMs(i);
        Assert.assertEquals(1, bads.size());
        Assert.assertEquals(vm1, bads.iterator().next());
    }*/

    /**
     * A preserve constraint asks for a minimum amount of resources but
     * their is no overbook ratio so, the default value of 1 is used
     * and vm1 or vm2 is moved to n2
     *
     * @throws org.btrplace.scheduler.SchedulerException
     */
    @Test
    public void testPreserveWithoutOverbook() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = mo.getMapping().on(n1, n2).run(n1, vm1, vm2).run(n2, vm3);
        ShareableResource rc = new ShareableResource("cpu", 10, 10);
        rc.setCapacity(n1, 7);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 5);

        Preserve pr = new Preserve(vm2, "cpu", 5);
        ChocoScheduler cra = new DefaultChocoScheduler();
        mo.attach(rc);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Online.newOnline(map.getAllNodes()));
        cstrs.add(pr);
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);

    }
}
