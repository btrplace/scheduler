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
 * Basic unit tests for {@link BootVM}.
 *
 * @author Fabien Hermenier
 */
public class BootVMTest {

    /**
     * Just boot a VM on a  node.
     */
    @Test
    public void testBasics() throws SchedulerException, ContradictionException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        final VM vm1 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addReadyVM(vm1);

        Parameters ps = new DefaultParameters();
        DurationEvaluators dev = ps.getDurationEvaluators();
        dev.register(org.btrplace.plan.event.BootVM.class, new ConstantActionDuration<>(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setParams(ps)
                .setNextVMsStates(new HashSet<>(), map.getAllVMs(), new HashSet<>(), new HashSet<>())
                .build();
        rp.getNodeActions().get(0).getState().instantiateTo(1, Cause.Null);
        rp.getNodeActions().get(1).getState().instantiateTo(1, Cause.Null);
        BootVM m = (BootVM) rp.getVMActions().get(0);
        Assert.assertEquals(vm1, m.getVM());
        Assert.assertNull(m.getCSlice());
        Assert.assertTrue(m.getDuration().isInstantiatedTo(5));
        Assert.assertTrue(m.getState().isInstantiatedTo(1));
        Assert.assertFalse(m.getDSlice().getHoster().isInstantiated());
        Assert.assertFalse(m.getDSlice().getStart().isInstantiated());
        Assert.assertFalse(m.getDSlice().getEnd().isInstantiated());
        new CMinMTTR().inject(ps, rp);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        org.btrplace.plan.event.BootVM a = (org.btrplace.plan.event.BootVM) p.getActions().iterator().next();

        Node dest = rp.getNode(m.getDSlice().getHoster().getValue());
        Assert.assertEquals(vm1, a.getVM());
        Assert.assertEquals(dest, a.getDestinationNode());
        Assert.assertEquals(a.getEnd() - a.getStart(), 5);
    }

    /**
     * Test that check when the action is shorter than the end of
     * the reconfiguration process.
     * In practice, 2 boot actions have to be executed sequentially
     */
    @Test
    public void testBootSequence() throws SchedulerException, ContradictionException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        final VM vm1 = mo.newVM();
        final VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addReadyVM(vm1);
        map.addReadyVM(vm2);

        Parameters ps = new DefaultParameters();
        DurationEvaluators dev = ps.getDurationEvaluators();
        dev.register(org.btrplace.plan.event.BootVM.class, new ConstantActionDuration<>(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setParams(ps)
                .setNextVMsStates(new HashSet<>(), map.getAllVMs(), new HashSet<>(), new HashSet<>())
                .build();
        BootVM m1 = (BootVM) rp.getVMActions().get(rp.getVM(vm1));
        BootVM m2 = (BootVM) rp.getVMActions().get(rp.getVM(vm2));
        rp.getNodeActions().get(0).getState().instantiateTo(1, Cause.Null);
        rp.getNodeActions().get(1).getState().instantiateTo(1, Cause.Null);
        rp.getModel().post(rp.getModel().arithm(m2.getStart(), ">=", m1.getEnd()));

        new CMinMTTR().inject(ps, rp);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        Iterator<Action> ite = p.iterator();
        org.btrplace.plan.event.BootVM b1 = (org.btrplace.plan.event.BootVM) ite.next();
        org.btrplace.plan.event.BootVM b2 = (org.btrplace.plan.event.BootVM) ite.next();
        Assert.assertEquals(vm1, b1.getVM());
        Assert.assertEquals(vm2, b2.getVM());
        Assert.assertTrue(b1.getEnd() <= b2.getStart());
        Assert.assertEquals(b1.getEnd() - b1.getStart(), 5);
        Assert.assertEquals(b2.getEnd() - b2.getStart(), 5);

    }
}
