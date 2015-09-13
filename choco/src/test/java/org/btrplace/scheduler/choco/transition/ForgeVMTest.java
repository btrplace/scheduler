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

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.*;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.DefaultParameters;
import org.btrplace.scheduler.choco.DefaultReconfigurationProblemBuilder;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.duration.ConstantActionDuration;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;

import java.util.Collections;


/**
 * Unit tests for {@link ForgeVM}.
 *
 * @author Fabien Hermenier
 */
public class ForgeVMTest {

    @Test
    public void testBasics() throws SchedulerException {
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        final VM vm1 = mo.newVM();

        mo.getAttributes().put(vm1, "template", "small");
        Parameters ps = new DefaultParameters();
        DurationEvaluators dev = ps.getDurationEvaluators();
        dev.register(org.btrplace.plan.event.ForgeVM.class, new ConstantActionDuration(7));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setParams(ps)
                .setNextVMsStates(Collections.singleton(vm1), Collections.<VM>emptySet(), Collections.<VM>emptySet(), Collections.<VM>emptySet())
                .build();
        ForgeVM ma = (ForgeVM) rp.getVMAction(vm1);
        Assert.assertEquals(vm1, ma.getVM());
        Assert.assertEquals(ma.getTemplate(), "small");
        Assert.assertTrue(ma.getDuration().isInstantiatedTo(7));
        Assert.assertFalse(ma.getStart().isInstantiated());
        Assert.assertFalse(ma.getEnd().isInstantiated());
        Assert.assertTrue(ma.getState().isInstantiatedTo(0));
        Assert.assertNull(ma.getCSlice());
        Assert.assertNull(ma.getDSlice());
    }

    @Test(expectedExceptions = {SchedulerException.class})
    public void testWithoutTemplate() throws SchedulerException {
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        final VM vm1 = mo.newVM();

        Parameters ps = new DefaultParameters();
        DurationEvaluators dev = ps.getDurationEvaluators();
        dev.register(org.btrplace.plan.event.ForgeVM.class, new ConstantActionDuration(7));
        new DefaultReconfigurationProblemBuilder(mo)
                .setParams(ps)
                .setNextVMsStates(Collections.singleton(vm1), Collections.<VM>emptySet(), Collections.<VM>emptySet(), Collections.<VM>emptySet())
                .build();

    }

    @Test
    public void testResolution() throws SchedulerException, ContradictionException {
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        final VM vm1 = mo.newVM();
        Node n1 = mo.newNode();

        m.addOnlineNode(n1);
        mo.getAttributes().put(vm1, "template", "small");
        Parameters ps = new DefaultParameters();
        DurationEvaluators dev = ps.getDurationEvaluators();
        dev.register(org.btrplace.plan.event.ForgeVM.class, new ConstantActionDuration(7));
        dev.register(ShutdownNode.class, new ConstantActionDuration(20));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setParams(ps)
                .setNextVMsStates(Collections.singleton(vm1), Collections.<VM>emptySet(), Collections.<VM>emptySet(), Collections.<VM>emptySet())
                .build();
        //Force the node to get offline
        ShutdownableNode n = (ShutdownableNode) rp.getNodeAction(n1);
        n.getState().instantiateTo(0, Cause.Null);
        System.out.println(rp.getSolver());
        ReconfigurationPlan p = rp.solve(0, false);

        Assert.assertNotNull(p);
        Assert.assertEquals(p.getDuration(), 20);
        for (Action a : p) {
            if (a instanceof org.btrplace.plan.event.ForgeVM) {
                org.btrplace.plan.event.ForgeVM action = (org.btrplace.plan.event.ForgeVM) a;
                Assert.assertTrue(p.getResult().getMapping().isReady(vm1));
                Assert.assertEquals(action.getVM(), vm1);
                Assert.assertEquals(action.getEnd(), 7);
                Assert.assertEquals(action.getStart(), 0);
            }
        }

    }

}
