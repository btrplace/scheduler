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
import org.btrplace.model.constraint.Ready;
import org.btrplace.model.constraint.Running;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.Seq;
import org.btrplace.model.constraint.Sleeping;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link CSequentialVMTransitions}.
 *
 * @author Fabien Hermenier
 */
public class CSeqTest {

    @Test
    public void testWithOnlyTransitions() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        Mapping map = mo.getMapping().on(n1, n2).ready(vm1).run(n1, vm2, vm4).sleep(n2, vm3);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Running(vm1));
        cstrs.add(new Sleeping(vm2));
        cstrs.add(new Running(vm3));
        cstrs.add(new Ready(vm4));
        cstrs.addAll(Online.newOnline(map.getAllNodes()));
        ChocoScheduler cra = new DefaultChocoScheduler();
        List<VM> seq = Arrays.asList(vm1, vm2, vm3, vm4);
        cstrs.add(new Seq(seq));
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
    }

    @Test
    public void testWithVMsWithNoTransitions() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        mo.getMapping().on(n1, n2).ready(vm1).run(n1, vm2, vm4).run(n2, vm3);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Running(vm1));
        cstrs.add(new Running(vm2));
        cstrs.add(new Running(vm3));
        cstrs.add(new Ready(vm4));
        ChocoScheduler cra = new DefaultChocoScheduler();
        List<VM> seq = Arrays.asList(vm1, vm2, vm3, vm4);
        cstrs.add(new Seq(seq));
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
        //System.out.println(plan);
    }
}
