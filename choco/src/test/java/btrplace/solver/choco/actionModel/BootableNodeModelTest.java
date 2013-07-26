/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.actionModel;

import btrplace.model.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.ShutdownNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultReconfigurationProblemBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.durationEvaluator.ConstantActionDuration;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;


/**
 * Unit tests for {@link BootableNodeModel}.
 *
 * @author Fabien Hermenier
 */
public class BootableNodeModelTest {

    @Test
    public void testBasic() throws SolverException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        Node n1 = mo.newNode();

        map.addOfflineNode(n1);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).build();
        BootableNodeModel na = (BootableNodeModel) rp.getNodeAction(n1);
        Assert.assertEquals(na.getNode(), n1);
    }

    @Test
    public void testForcingBoot() throws SolverException, ContradictionException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        Node n1 = mo.newNode();

        map.addOfflineNode(n1);

        DurationEvaluators dev = new DurationEvaluators();
        dev.register(BootNode.class, new ConstantActionDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .labelVariables()
                .setDurationEvaluators(dev)
                .build();
        BootableNodeModel na = (BootableNodeModel) rp.getNodeAction(n1);
        na.getState().setVal(1);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(na.getDuration().getVal(), 5);
        Assert.assertEquals(na.getStart().getVal(), 0);
        Assert.assertEquals(na.getEnd().getVal(), 5);
        Assert.assertEquals(na.getHostingStart().getVal(), 5);
        Assert.assertEquals(na.getHostingEnd().getVal(), 5);


        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().isOnline(n1));
    }

    @Test
    public void testForcingOffline() throws SolverException, ContradictionException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        Node n1 = mo.newNode();
        map.addOfflineNode(n1);

        DurationEvaluators dev = new DurationEvaluators();
        dev.register(BootNode.class, new ConstantActionDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .labelVariables()
                .setDurationEvaluators(dev)
                .build();
        BootableNodeModel na = (BootableNodeModel) rp.getNodeAction(n1);
        na.getState().setVal(0);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        Assert.assertEquals(na.getDuration().getVal(), 0);
        Assert.assertEquals(na.getStart().getVal(), 0);
        Assert.assertEquals(na.getEnd().getVal(), 0);
        Assert.assertEquals(na.getHostingStart().getVal(), rp.getEnd().getVal());
        Assert.assertEquals(na.getHostingEnd().getVal(), rp.getEnd().getVal());

        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 0);
        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n1));
    }

    @Test
    public void testRequiredOnline() throws SolverException, ContradictionException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        VM vm1 = mo.newVM();
        Node n1 = mo.newNode();

        map.addOfflineNode(n1);
        map.addReadyVM(vm1);

        DurationEvaluators dev = new DurationEvaluators();
        dev.register(BootNode.class, new ConstantActionDuration(5));
        dev.register(BootVM.class, new ConstantActionDuration(2));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(Collections.<VM>emptySet(), Collections.singleton(vm1), Collections.<VM>emptySet(), Collections.<VM>emptySet())
                .labelVariables()
                .setDurationEvaluators(dev)
                .build();

        BootableNodeModel na = (BootableNodeModel) rp.getNodeAction(n1);
        Assert.assertNotNull(rp.solve(0, false));
        Assert.assertEquals(na.getStart().getVal(), 0);
        Assert.assertEquals(na.getEnd().getVal(), 5);
        Assert.assertEquals(na.getHostingStart().getVal(), 5);
        Assert.assertEquals(na.getHostingEnd().getVal(), 7);
    }

    @Test
    public void testBootCascade() throws SolverException, ContradictionException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        map.addOfflineNode(n1);
        map.addOfflineNode(n2);

        DurationEvaluators dev = new DurationEvaluators();
        dev.register(BootNode.class, new ConstantActionDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .labelVariables()
                .setDurationEvaluators(dev)
                .build();
        BootableNodeModel na1 = (BootableNodeModel) rp.getNodeAction(n1);
        BootableNodeModel na2 = (BootableNodeModel) rp.getNodeAction(n2);
        na1.getState().setVal(1);
        na2.getState().setVal(1);
        CPSolver solver = rp.getSolver();
        solver.post(solver.eq(na1.getEnd(), na2.getStart()));
        Assert.assertNotNull(rp.solve(0, false));
    }

    @Test
    public void testDelayedBooting() throws ContradictionException, SolverException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        Node n2 = mo.newNode();

        map.addOfflineNode(n2);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(BootNode.class, new ConstantActionDuration(2));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setDurationEvaluators(dev)
                .labelVariables()
                .build();
        BootableNodeModel ma2 = (BootableNodeModel) rp.getNodeAction(n2);
        ma2.getState().setVal(1);
        ma2.getStart().setInf(5);
        ReconfigurationPlan p = rp.solve(0, false);
        ChocoLogging.flushLogs();
        Assert.assertNotNull(p);
        System.out.println(p);
        System.out.flush();
    }

    /**
     * Unit test for issue #3
     */
    @Test
    public void testActionDurationSimple() throws SolverException, ContradictionException {
        Model model = new DefaultModel();
        Mapping map = model.getMapping();
        Node n1 = model.newNode();
        Node n4 = model.newNode();

        map.addOnlineNode(n1);
        map.addOfflineNode(n4);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(ShutdownNode.class, new ConstantActionDuration(5));
        dev.register(BootNode.class, new ConstantActionDuration(3));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model)
                .setDurationEvaluators(dev)
                .labelVariables()
                .build();

        ShutdownableNodeModel sn1 = (ShutdownableNodeModel) rp.getNodeAction(n1);
        sn1.getState().setVal(0);
        BootableNodeModel bn4 = (BootableNodeModel) rp.getNodeAction(n4);
        bn4.getState().setVal(0);

        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(bn4.getStart().getVal(), 0);
        Assert.assertEquals(bn4.getDuration().getVal(), 0);
        Assert.assertEquals(bn4.getEnd().getVal(), 0);
        Assert.assertEquals(bn4.getHostingStart().getVal(), 0);
        Assert.assertEquals(bn4.getHostingEnd().getVal(), 0);
        Assert.assertEquals(p.getSize(), 1);
        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n1));
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n4));
    }
}
