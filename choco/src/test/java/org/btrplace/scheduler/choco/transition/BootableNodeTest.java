/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
import org.btrplace.model.constraint.Running;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.BootNode;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.*;
import org.btrplace.scheduler.choco.constraint.mttr.CMinMTTR;
import org.btrplace.scheduler.choco.duration.ConstantActionDuration;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;


/**
 * Unit tests for {@link BootableNode}.
 *
 * @author Fabien Hermenier
 */
public class BootableNodeTest {

    @Test
    public void testBasic() throws SchedulerException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        Node n1 = mo.newNode();

        map.addOfflineNode(n1);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).build();
        BootableNode na = (BootableNode) rp.getNodeAction(n1);
        Assert.assertEquals(na.getNode(), n1);
    }

    @Test
    public void testForcingBoot() throws SchedulerException, ContradictionException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        Node n1 = mo.newNode();

        map.addOfflineNode(n1);

        Parameters ps = new DefaultParameters();
        DurationEvaluators dev = ps.getDurationEvaluators();
        dev.register(BootNode.class, new ConstantActionDuration<>(5));

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setParams(ps)
                .build();
        BootableNode na = (BootableNode) rp.getNodeAction(n1);
        na.getState().instantiateTo(1, Cause.Null);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(na.getDuration().getValue(), 5);
        Assert.assertEquals(na.getStart().getValue(), 0);
        Assert.assertEquals(na.getEnd().getValue(), 5);
        Assert.assertEquals(na.getHostingStart().getValue(), 5);
        Assert.assertEquals(na.getHostingEnd().getValue(), 5);


        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().isOnline(n1));
    }

    @Test
    public void testForcingOffline() throws SchedulerException, ContradictionException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        Node n1 = mo.newNode();
        map.addOfflineNode(n1);

        Parameters ps = new DefaultParameters();
        DurationEvaluators dev = ps.getDurationEvaluators();
        dev.register(BootNode.class, new ConstantActionDuration<>(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setParams(ps)
                .build();
        BootableNode na = (BootableNode) rp.getNodeAction(n1);
        na.getState().instantiateTo(0, Cause.Null);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        Assert.assertEquals(na.getDuration().getValue(), 0);
        Assert.assertEquals(na.getStart().getValue(), 0);
        Assert.assertEquals(na.getEnd().getValue(), 0);
        Assert.assertEquals(na.getHostingStart().getValue(), rp.getEnd().getValue());
        Assert.assertEquals(na.getHostingEnd().getValue(), rp.getEnd().getValue());

        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 0);
        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n1));
    }

    @Test
    public void testRequiredOnline() throws SchedulerException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        VM vm1 = mo.newVM();
        Node n1 = mo.newNode();

        map.addOfflineNode(n1);
        map.addReadyVM(vm1);

        ChocoScheduler s = new DefaultChocoScheduler();
        Parameters ps = s.getParameters();
        DurationEvaluators dev = ps.getDurationEvaluators();
        dev.register(BootNode.class, new ConstantActionDuration<>(5));
        dev.register(BootVM.class, new ConstantActionDuration<>(2));

        ReconfigurationPlan plan = s.solve(mo, Collections.singleton(new Running(vm1)));
        Assert.assertNotNull(plan);
        Assert.assertEquals(plan.getSize(), 2);
        for (Action a : plan.getActions()) {
            if (a instanceof BootNode) {
                Assert.assertEquals(a.getStart(), 0);
                Assert.assertEquals(a.getEnd(), 5);
            } else {
                Assert.assertEquals(a.getStart(), 5);
                Assert.assertEquals(a.getEnd(), 7);
            }
        }
    }

    @Test
    public void testBootCascade() throws SchedulerException, ContradictionException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        map.addOfflineNode(n1);
        map.addOfflineNode(n2);

        Parameters ps = new DefaultParameters();
        DurationEvaluators dev = ps.getDurationEvaluators();
        dev.register(BootNode.class, new ConstantActionDuration<>(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setParams(ps)
                .build();
        BootableNode na1 = (BootableNode) rp.getNodeAction(n1);
        BootableNode na2 = (BootableNode) rp.getNodeAction(n2);
        na1.getState().instantiateTo(1, Cause.Null);
        na2.getState().instantiateTo(1, Cause.Null);
        Solver solver = rp.getSolver();
        solver.post(IntConstraintFactory.arithm(na1.getEnd(), "=", na2.getEnd()));
        new CMinMTTR().inject(ps, rp);
        Assert.assertNotNull(rp.solve(0, false));
    }

    @Test
    public void testDelayedBooting() throws ContradictionException, SchedulerException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        Node n2 = mo.newNode();

        map.addOfflineNode(n2);
        Parameters ps = new DefaultParameters();
        DurationEvaluators dev = ps.getDurationEvaluators();
        dev.register(BootNode.class, new ConstantActionDuration<>(2));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setParams(ps)
                .build();
        BootableNode ma2 = (BootableNode) rp.getNodeAction(n2);
        ma2.getState().instantiateTo(1, Cause.Null);
        ma2.getStart().updateLowerBound(5, Cause.Null);
        new CMinMTTR().inject(ps, rp);
        ReconfigurationPlan p = rp.solve(0, false);
        //ChocoLogging.flushLogs();
        Assert.assertNotNull(p);
        System.out.println(p);
        System.out.flush();
    }

    /**
     * Unit test for issue #3
     */
    @Test
    public void testActionDurationSimple() throws SchedulerException, ContradictionException {
        Model model = new DefaultModel();
        Mapping map = model.getMapping();
        Node n1 = model.newNode();
        Node n4 = model.newNode();

        map.addOnlineNode(n1);
        map.addOfflineNode(n4);
        Parameters ps = new DefaultParameters();
        DurationEvaluators dev = ps.getDurationEvaluators();
        dev.register(ShutdownNode.class, new ConstantActionDuration<>(5));
        dev.register(BootNode.class, new ConstantActionDuration<>(3));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model)
                .setParams(ps)
                .build();

        ShutdownableNode sn1 = (ShutdownableNode) rp.getNodeAction(n1);
        sn1.getState().instantiateTo(0, Cause.Null);
        BootableNode bn4 = (BootableNode) rp.getNodeAction(n4);
        bn4.getState().instantiateTo(0, Cause.Null);

        new CMinMTTR().inject(ps, rp);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(bn4.getStart().getValue(), 0);
        Assert.assertEquals(bn4.getDuration().getValue(), 0);
        Assert.assertEquals(bn4.getEnd().getValue(), 0);
        Assert.assertEquals(bn4.getHostingStart().getValue(), 0);
        Assert.assertEquals(bn4.getHostingEnd().getValue(), 0);
        Assert.assertEquals(p.getSize(), 1);
        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n1));
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n4));
    }
}
