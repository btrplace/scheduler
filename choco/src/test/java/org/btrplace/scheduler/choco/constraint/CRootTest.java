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
import org.btrplace.model.constraint.Root;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link CRoot}.
 *
 * @author Fabien Hermenier
 */
public class CRootTest {

    @Test
    public void testBasic() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = mo.getMapping().on(n1, n2).run(n1, vm1, vm2).ready(vm3);

        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.doRepair(false);
        Root r1 = new Root(vm1);
        List<SatConstraint> l = new ArrayList<>();
        l.add(r1);
        l.addAll(Online.newOnline(map.getAllNodes()));
        ReconfigurationPlan p = cra.solve(mo, l);
        Assert.assertNotNull(p);
        Model res = p.getResult();
        Assert.assertEquals(n1, res.getMapping().getVMLocation(vm1));
    }
}
