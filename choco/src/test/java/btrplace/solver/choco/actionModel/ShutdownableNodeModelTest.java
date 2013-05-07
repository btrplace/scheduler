/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.ShutdownNode;
import btrplace.plan.event.ShutdownVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultReconfigurationProblemBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import btrplace.test.PremadeElements;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.UUID;

/**
 * Unit tests for {@link ShutdownableNodeModel}.
 *
 * @author Fabien hermenier
 */
public class ShutdownableNodeModelTest implements PremadeElements {

    @Test
    public void testBasics() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        Model mo = new DefaultModel(map);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .labelVariables()
                .build();
        ShutdownableNodeModel ma = (ShutdownableNodeModel) rp.getNodeAction(n1);
        Assert.assertEquals(ma.getNode(), n1);
        Assert.assertEquals(ma.getHostingStart(), rp.getStart());
    }

    @Test
    public void testForcedOnline() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOfflineNode(n2);
        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(ShutdownNode.class, new ConstantDuration(5));
        dev.register(BootNode.class, new ConstantDuration(10));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setDurationEvaluatators(dev)
                .labelVariables()
                .build();
        ShutdownableNodeModel ma = (ShutdownableNodeModel) rp.getNodeAction(n1);
        ma.getState().setVal(1);   //stay online

        //To make the result plan 10 seconds long
        BootableNodeModel ma2 = (BootableNodeModel) rp.getNodeAction(n2);
        ma2.getState().setVal(1); //go online
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(ma.getDuration().getVal(), 0);
        Assert.assertEquals(ma.getStart().getVal(), 0);
        Assert.assertEquals(ma.getEnd().getVal(), 0);
        Assert.assertEquals(ma.getHostingStart().getVal(), 0);
        Assert.assertEquals(ma.getHostingEnd().getVal(), 10);


        Assert.assertEquals(p.getSize(), 1);
        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().getOnlineNodes().contains(n1));
    }

    @Test
    public void testForcedOffline() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(ShutdownNode.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setDurationEvaluatators(dev)
                .labelVariables()
                .build();
        ShutdownableNodeModel ma = (ShutdownableNodeModel) rp.getNodeAction(n1);
        ma.getState().setVal(0);

        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        Assert.assertEquals(ma.getDuration().getVal(), 5);
        Assert.assertEquals(ma.getStart().getVal(), 0);
        Assert.assertEquals(ma.getEnd().getVal(), 5);
        Assert.assertEquals(ma.getHostingStart().getVal(), 0);
        Assert.assertEquals(ma.getHostingEnd().getVal(), 0);

        Assert.assertEquals(p.getSize(), 1);
        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n1));
    }

    @Test
    public void testScheduledShutdown() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(ShutdownVM.class, new ConstantDuration(2));
        dev.register(ShutdownNode.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setDurationEvaluatators(dev)
                .labelVariables()
                .setNextVMsStates(Collections.singleton(vm1), Collections.<UUID>emptySet(), Collections.<UUID>emptySet(), Collections.<UUID>emptySet())
                .build();
        ShutdownableNodeModel ma = (ShutdownableNodeModel) rp.getNodeAction(n1);
        ma.getState().setVal(0);

        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        Assert.assertEquals(ma.getState().getVal(), 0);
        Assert.assertEquals(ma.getDuration().getVal(), 5);
        Assert.assertEquals(ma.getStart().getVal(), 2);
        Assert.assertEquals(ma.getEnd().getVal(), 7);
        Assert.assertEquals(ma.getHostingStart().getVal(), 0);
        Assert.assertEquals(ma.getHostingEnd().getVal(), 2);


        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n1));
    }

    /**
     * The 2 nodes are set offline but n2 will consume being offline after n1
     *
     * @throws SolverException
     * @throws ContradictionException
     */
    @Test
    public void testCascadedShutdown() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(ShutdownVM.class, new ConstantDuration(2));
        dev.register(ShutdownNode.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setDurationEvaluatators(dev)
                .labelVariables()
                .build();
        ShutdownableNodeModel ma1 = (ShutdownableNodeModel) rp.getNodeAction(n1);
        ShutdownableNodeModel ma2 = (ShutdownableNodeModel) rp.getNodeAction(n2);
        ma1.getState().setVal(0);
        ma2.getState().setVal(0);

        CPSolver solver = rp.getSolver();
        solver.post(solver.eq(ma2.getStart(), ma1.getEnd()));

        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(ma1.getStart().getVal(), 0);
        Assert.assertEquals(ma2.getStart().getVal(), ma1.getEnd().getVal());
        Model res = p.getResult();
        Assert.assertEquals(res.getMapping().getOfflineNodes().size(), 2);
    }

    @Test
    public void testShutdownBeforeVMsLeave() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(ShutdownVM.class, new ConstantDuration(2));
        dev.register(ShutdownNode.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(Collections.singleton(vm1), Collections.<UUID>emptySet(), Collections.<UUID>emptySet(), Collections.<UUID>emptySet())
                .setDurationEvaluatators(dev)
                .labelVariables()
                .build();
        ShutdownableNodeModel ma1 = (ShutdownableNodeModel) rp.getNodeAction(n1);
        ma1.getState().setVal(0);
        ma1.getHostingEnd().setVal(0);
        rp.getEnd().setSup(10);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNull(p);
        System.out.println(p);
    }

    @Test
    public void testSwitchState() throws ContradictionException, SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOfflineNode(n2);
        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(BootNode.class, new ConstantDuration(2));
        dev.register(ShutdownNode.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setDurationEvaluatators(dev)
                .labelVariables()
                .build();
        ShutdownableNodeModel ma1 = (ShutdownableNodeModel) rp.getNodeAction(n1);
        BootableNodeModel ma2 = (BootableNodeModel) rp.getNodeAction(n2);
        ma1.getState().setVal(0);
        ma2.getState().setVal(1);
        CPSolver solver = rp.getSolver();
        solver.post(solver.eq(ma1.getEnd(), ma2.getStart()));
        ReconfigurationPlan p = rp.solve(0, false);
        ChocoLogging.flushLogs();
        Assert.assertNotNull(p);
        System.out.println(p);
        System.out.flush();
    }

    /**
     * Issue #2 about NodeActionModel.
     *
     * @throws SolverException
     * @throws ContradictionException
     */
    @Test
    public void testNodeHostingEnd() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);
        Model model = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(ShutdownNode.class, new ConstantDuration(5));
        dev.register(BootNode.class, new ConstantDuration(10));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model)
                .setDurationEvaluatators(dev)
                .labelVariables()
                .build();
        ShutdownableNodeModel shd = (ShutdownableNodeModel) rp.getNodeAction(n1);
        shd.getState().setVal(1); //Stay online

        ShutdownableNodeModel shd2 = (ShutdownableNodeModel) rp.getNodeAction(n2);
        shd2.getState().setVal(0);  //Go offline
        shd2.getStart().setVal(1); //Start going offline at 1

        BootableNodeModel bn = (BootableNodeModel) rp.getNodeAction(n3);
        bn.getState().setVal(1); //Go online
        bn.getStart().setVal(6); //Start going online at 6
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(shd.getDuration().getVal(), 0);
        Assert.assertEquals(shd.getStart().getVal(), 0);
        Assert.assertEquals(shd.getEnd().getVal(), 0);
        Assert.assertEquals(shd.getHostingStart().getVal(), 0);
        Assert.assertEquals(shd.getHostingEnd().getVal(), 16);
        Assert.assertEquals(shd2.getDuration().getVal(), 5);
        Assert.assertEquals(shd2.getStart().getVal(), 1);
        Assert.assertEquals(shd2.getEnd().getVal(), 6);
        Assert.assertEquals(shd2.getHostingStart().getVal(), 0);
        Assert.assertEquals(shd2.getHostingEnd().getVal(), 1);
        Assert.assertEquals(bn.getStart().getVal(), 6);
        Assert.assertEquals(bn.getDuration().getVal(), 10);
        Assert.assertEquals(bn.getEnd().getVal(), 16);
        Assert.assertEquals(bn.getHostingStart().getVal(), 16);
        Assert.assertEquals(bn.getHostingEnd().getVal(), 16);
        Assert.assertEquals(p.getSize(), 2);
        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().getOnlineNodes().contains(n1));
        Assert.assertTrue(res.getMapping().getOnlineNodes().contains(n3));
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n2));
    }

    @Test
    public void testActionDurationSimple() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOfflineNode(n4);
        Model model = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(ShutdownNode.class, new ConstantDuration(5));
        dev.register(BootNode.class, new ConstantDuration(3));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model)
                .setDurationEvaluatators(dev)
                .labelVariables()
                .build();

        ShutdownableNodeModel sn1 = (ShutdownableNodeModel) rp.getNodeAction(n1);
        sn1.getState().setVal(0);
        BootableNodeModel bn4 = (BootableNodeModel) rp.getNodeAction(n4);
        bn4.getState().setVal(0);

        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(rp.getStart().getVal(), 0);
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

    @Test
    public void testShutdownable() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n4);
        Model model = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(ShutdownNode.class, new ConstantDuration(5));
        dev.register(BootNode.class, new ConstantDuration(3));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model)
                .setDurationEvaluatators(dev)
                .labelVariables()
                .build();

        ShutdownableNodeModel sn1 = (ShutdownableNodeModel) rp.getNodeAction(n1);
        sn1.getState().setVal(0);
        sn1.getStart().setVal(2);
        ShutdownableNodeModel sn4 = (ShutdownableNodeModel) rp.getNodeAction(n4);
        sn4.getState().setVal(1);

        ReconfigurationPlan p = rp.solve(0, false);

        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(rp.getStart().getVal(), 0);
        Assert.assertEquals(rp.getEnd().getVal(), 7);

        Assert.assertEquals(sn1.getStart().getVal(), 2);
        Assert.assertEquals(sn1.getDuration().getVal(), 5);
        Assert.assertEquals(sn1.getEnd().getVal(), 7);
        Assert.assertEquals(sn1.getHostingStart().getVal(), 0);
        Assert.assertEquals(sn1.getHostingEnd().getVal(), 2);

        Assert.assertEquals(rp.getStart().getVal(), 0);
        Assert.assertEquals(sn4.getStart().getVal(), 0);
        Assert.assertEquals(sn4.getDuration().getVal(), 0);
        Assert.assertEquals(sn4.getEnd().getVal(), 0);
        Assert.assertEquals(sn4.getHostingStart().getVal(), 0);
        Assert.assertEquals(sn4.getHostingEnd().getVal(), 7);
        Assert.assertEquals(p.getSize(), 1);
        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().getOnlineNodes().contains(n4));
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n1));
    }
}
