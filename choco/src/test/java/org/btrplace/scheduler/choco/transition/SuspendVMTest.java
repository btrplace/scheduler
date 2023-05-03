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
import java.util.Iterator;


/**
 * Unit tests for {@link SuspendVM}.
 *
 * @author Fabien Hermenier
 */
public class SuspendVMTest {

    @Test
    public void testBasic() throws ContradictionException, SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        Node n1 = mo.newNode();

        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);

        Parameters ps = new DefaultParameters();
        DurationEvaluators dev = ps.getDurationEvaluators();
        dev.register(org.btrplace.plan.event.SuspendVM.class, new ConstantActionDuration<>(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setParams(ps)
                .setNextVMsStates(new HashSet<>(), new HashSet<>(), map.getAllVMs(), new HashSet<>())
                .build();
        rp.getNodeActions().get(0).getState().instantiateTo(1, Cause.Null);
        SuspendVM m = (SuspendVM) rp.getVMActions().get(0);
        Assert.assertEquals(vm1, m.getVM());
        Assert.assertNull(m.getDSlice());
        Assert.assertTrue(m.getDuration().isInstantiatedTo(5));
        Assert.assertTrue(m.getState().isInstantiatedTo(0));
        Assert.assertTrue(m.getCSlice().getHoster().isInstantiatedTo(0));

        new CMinMTTR().inject(ps, rp);
        ReconfigurationPlan p = rp.solve(0, false);
        org.btrplace.plan.event.SuspendVM a = (org.btrplace.plan.event.SuspendVM) p.getActions().iterator().next();
        Assert.assertEquals(n1, a.getSourceNode());
        Assert.assertEquals(vm1, a.getVM());
        Assert.assertEquals(a.getEnd() - a.getStart(), 5);
    }

    /**
     * Test that check that the action duration is lesser than
     * the cSlice duration. This allows actions scheduling
     * In practice, for this test, 2 suspend actions have to be executed sequentially
     */
    @Test
    public void testSuspendSequences() throws SchedulerException, ContradictionException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();

        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);

        Parameters ps = new DefaultParameters();
        DurationEvaluators dev = ps.getDurationEvaluators();
        dev.register(org.btrplace.plan.event.SuspendVM.class, new ConstantActionDuration<>(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setParams(ps)
                .setNextVMsStates(new HashSet<>(), new HashSet<>(), map.getAllVMs(), new HashSet<>())
                .build();
        SuspendVM m1 = (SuspendVM) rp.getVMActions().get(rp.getVM(vm1));
        SuspendVM m2 = (SuspendVM) rp.getVMActions().get(rp.getVM(vm2));
        rp.getNodeActions().get(0).getState().instantiateTo(1, Cause.Null);
        org.chocosolver.solver.Model csp = rp.getModel();
        csp.post(csp.arithm(m2.getStart(), ">=", m1.getEnd()));

        new CMinMTTR().inject(ps, rp);
        ReconfigurationPlan p = rp.solve(0, false);

        Assert.assertNotNull(p);
        Iterator<Action> ite = p.iterator();
        org.btrplace.plan.event.SuspendVM b1 = (org.btrplace.plan.event.SuspendVM) ite.next();
        org.btrplace.plan.event.SuspendVM b2 = (org.btrplace.plan.event.SuspendVM) ite.next();
        Assert.assertEquals(vm1, b1.getVM());
        Assert.assertEquals(vm2, b2.getVM());
        Assert.assertTrue(b1.getEnd() <= b2.getStart());
        Assert.assertEquals(b1.getEnd() - b1.getStart(), 5);
        Assert.assertEquals(b2.getEnd() - b2.getStart(), 5);

    }
}
