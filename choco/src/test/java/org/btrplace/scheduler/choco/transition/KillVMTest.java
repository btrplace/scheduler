/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.*;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.DefaultParameters;
import org.btrplace.scheduler.choco.DefaultReconfigurationProblemBuilder;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.constraint.mttr.CMinMTTR;
import org.btrplace.scheduler.choco.duration.ConstantActionDuration;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;


/**
 * Unit tests for {@link KillVM}.
 *
 * @author Fabien Hermenier
 */
public class KillVMTest {

    /**
     * Test the action model with different action models.
     */
    @Test
    public void testBasics() throws ContradictionException, SchedulerException {
        Model mo = new DefaultModel();

        Mapping map = mo.getMapping();

        Node n1 = mo.newNode();
        map.addOnlineNode(n1);
        VM vm1 = mo.newVM();
        map.addRunningVM(vm1, n1);

        VM vm2 = mo.newVM();
        map.addReadyVM(vm2);
        VM vm3 = mo.newVM();
        map.addSleepingVM(vm3, n1);

        Set<VM> empty = new HashSet<>();
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(org.btrplace.plan.event.KillVM.class, new ConstantActionDuration<>(1));
        Parameters ps = new DefaultParameters();
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(empty, empty, empty, map.getAllVMs())
                .setParams(ps)
                .build();


        rp.getNodeAction(n1).getState().instantiateTo(rp.getVM(vm1), Cause.Null);
        //Common stuff
        for (VM vm : map.getAllVMs()) {
            KillVM m = (KillVM) rp.getVMAction(vm);
            Assert.assertEquals(vm, m.getVM());
            Assert.assertTrue(m.getState().isInstantiatedTo(0));
            Assert.assertNull(m.getDSlice());
            Assert.assertTrue(m.getDuration().isInstantiatedTo(1));
            Assert.assertTrue(m.getStart().isInstantiatedTo(0));
            Assert.assertTrue(m.getEnd().isInstantiatedTo(1));
        }

        //The waiting and the sleeping VM have no CSlice
        Assert.assertNull(rp.getVMAction(vm2).getCSlice());
        Assert.assertNull(rp.getVMAction(vm3).getCSlice());

        //The running VM has a CSlice
        Assert.assertNotNull(rp.getVMAction(vm1).getCSlice());
        Assert.assertTrue(rp.getVMAction(vm1).getCSlice().getHoster().isInstantiatedTo(rp.getNode(n1)));
        new CMinMTTR().inject(ps, rp);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);

        for (Action a : p) {
            if (a instanceof org.btrplace.plan.event.KillVM) {
                org.btrplace.plan.event.KillVM vma = (org.btrplace.plan.event.KillVM) a;
                Assert.assertEquals(a.getEnd(), 1);
                Assert.assertEquals(a.getStart(), 0);
                if (vma.getVM().equals(vm1) || vma.getVM().equals(vm3)) {
                    Assert.assertEquals(vma.getNode(), n1);
                } else if (vma.getVM().equals(vm2)) {
                    Assert.assertNull(vma.getNode());
                } else {
                    Assert.fail();
                }
            }
        }
    }

}
