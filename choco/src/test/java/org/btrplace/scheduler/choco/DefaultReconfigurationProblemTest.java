/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco;

import org.btrplace.model.*;
import org.btrplace.model.view.ModelView;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.UnstatableProblemException;
import org.btrplace.scheduler.choco.constraint.mttr.CMinMTTR;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.btrplace.scheduler.choco.transition.*;
import org.btrplace.scheduler.choco.view.ChocoView;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Unit tests for {@link DefaultReconfigurationProblem}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationProblemTest {

    public class MockCView implements ChocoView {
        @Override
        public String getIdentifier() {
            return "cmock";
        }

        @Override
        public boolean beforeSolve(ReconfigurationProblem rp) {
            return true;
        }

        @Override
        public boolean insertActions(ReconfigurationProblem rp, Solution s, ReconfigurationPlan p) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean cloneVM(VM vm, VM clone) {
            throw new UnsupportedOperationException();
        }
    }

    public class MockView implements ModelView {
        @Override
        public String getIdentifier() {
            return "mock";
        }

        @Override
        public ModelView copy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean substituteVM(VM curId, VM nextId) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Just test the state definition of the actions.
     *
     * @throws org.btrplace.scheduler.SchedulerException should not occur
     */
    @Test
    public void testSimplestInstantiation() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addSleepingVM(vm4, n2);
        map.addReadyVM(vm5);
        map.addReadyVM(vm6);
        VM vm7 = mo.newVM();
        Set<VM> toRun = new HashSet<>();
        Set<VM> toWait = new HashSet<>();
        toWait.add(vm6);
        toWait.add(vm7);
        toRun.add(vm5);
        toRun.add(vm4);
        toRun.add(vm1);
        mo.getAttributes().put(vm7, "template", "small");
        Parameters ps = new DefaultParameters();
        DurationEvaluators dEval = ps.getDurationEvaluators();
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(toWait, toRun, Collections.singleton(vm3), Collections.singleton(vm2))
                .setParams(ps)
                .build();

        Assert.assertEquals(dEval, rp.getDurationEvaluators());
        Assert.assertEquals(rp.getFutureReadyVMs(), toWait);
        Assert.assertEquals(rp.getFutureRunningVMs(), toRun);
        Assert.assertEquals(rp.getFutureSleepingVMs(), Collections.singleton(vm3));
        Assert.assertEquals(rp.getFutureKilledVMs(), Collections.singleton(vm2));
        Assert.assertEquals(rp.getVMs().size(), 7);
        Assert.assertEquals(rp.getNodes().size(), 3);
        Assert.assertEquals(rp.getManageableVMs().size(), rp.getVMs().size(), rp.getManageableVMs().toString());
        Assert.assertTrue(rp.getStart().isInstantiated() && rp.getStart().getValue() == 0);

        //Test the index values of the nodes and the VMs.
        for (int i = 0; i < rp.getVMs().size(); i++) {
            VM vm = rp.getVM(i);
            Assert.assertEquals(i, rp.getVM(vm));
        }
        Assert.assertEquals(rp.getVM(mo.newVM()), -1);

        for (int i = 0; i < rp.getNodes().size(); i++) {
            Node n = rp.getNode(i);
            Assert.assertEquals(i, rp.getNode(n));
        }
        Assert.assertEquals(rp.getNode(mo.newNode()), -1);
    }

    @Test
    public void testManageableVMs() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addSleepingVM(vm4, n2);
        map.addReadyVM(vm5);
        map.addReadyVM(vm6);

        Set<VM> runnings = new HashSet<>(map.getRunningVMs());
        runnings.add(vm6);
        runnings.add(vm5);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(Collections.emptySet(), runnings, map.getSleepingVMs(), Collections.emptySet())
                .setManageableVMs(map.getRunningVMs(n1)).build();
        /*
          vm1: running -> running
          vm2: running-> running
          vm3: running -> running
          vm4: sleeping -> sleeping
          vm5: ready -> running
          vm6: ready -> running

         * manageable_runnings: vm1, vm2
         * manageable: vm1, vm2, vm5, vm6 (with ids: vm#0, vm#1, vm#4, vm#5)
         */
        Set<VM> manageable = rp.getManageableVMs();

        Assert.assertEquals(manageable.size(), 4, manageable.toString());
        Assert.assertTrue(manageable.containsAll(Arrays.asList(vm6, vm5, vm1, vm2)));
        //Check the action model that has been used for each of the VM.
        for (VM vm : map.getAllVMs()) {
            Assert.assertEquals(manageable.contains(vm), rp.getVMAction(vm).isManaged());
        }
    }


    @Test
    public void testVMToWaiting() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        //map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addSleepingVM(vm4, n2);
        map.addReadyVM(vm5);
        map.addReadyVM(vm6);
        mo.getAttributes().put(vm1, "template", "small");
        ReconfigurationProblem rp =
                new DefaultReconfigurationProblemBuilder(mo)
                        .setNextVMsStates(Collections.singleton(vm1),
                                new HashSet<>(),
                                new HashSet<>(),
                                new HashSet<>()).build();

        VMTransition a = rp.getVMActions().get(rp.getVM(vm1));
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(ForgeVM.class, a.getClass());
    }

    @Test
    public void testWaitingVMToRun() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addSleepingVM(vm4, n2);
        map.addReadyVM(vm5);
        map.addReadyVM(vm6);
        Mapping m = mo.getMapping();
        m.addReadyVM(vm1);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(new HashSet<>(),
                        Collections.singleton(vm1),
                        new HashSet<>(),
                        new HashSet<>()).build();

        VMTransition a = rp.getVMActions().get(0);
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(BootVM.class, a.getClass());
    }

    @Test
    public void testVMStayRunning() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addSleepingVM(vm4, n2);
        map.addReadyVM(vm5);
        map.addReadyVM(vm6);
        Mapping m = mo.getMapping();
        m.addOnlineNode(n1);
        m.addRunningVM(vm1, n1);

        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(new HashSet<>(),
                        Collections.singleton(vm1),
                        new HashSet<>(),
                        new HashSet<>()).build();
        VMTransition a = rp.getVMActions().get(0);
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(RelocatableVM.class, a.getClass());
    }

    @Test
    public void testVMRunningToSleeping() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addSleepingVM(vm4, n2);
        map.addReadyVM(vm5);
        map.addReadyVM(vm6);
        Mapping m = mo.getMapping();
        m.addOnlineNode(n1);
        m.addRunningVM(vm1, n1);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(new HashSet<>(),
                        new HashSet<>(),
                        Collections.singleton(vm1),
                        new HashSet<>()).build();

        VMTransition a = rp.getVMActions().get(0);
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(SuspendVM.class, a.getClass());
    }

    @Test
    public void testVMsToKill() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addSleepingVM(vm4, n2);
        map.addReadyVM(vm5);
        map.addReadyVM(vm6);
        Mapping m = mo.getMapping();
        m.addOnlineNode(n1);
        m.addRunningVM(vm1, n1);
        m.addSleepingVM(vm2, n1);
        m.addReadyVM(vm3);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>(),
                        m.getAllVMs()).build();

        for (VMTransition a : rp.getVMActions()) {
            Assert.assertEquals(a.getClass(), KillVM.class);
        }
    }

    @Test
    public void testVMToShutdown() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addSleepingVM(vm4, n2);
        map.addReadyVM(vm5);
        map.addReadyVM(vm6);
        Mapping m = mo.getMapping();
        m.addOnlineNode(n1);
        m.addRunningVM(vm1, n1);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(Collections.singleton(vm1),
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>()).build();
        VMTransition a = rp.getVMActions().get(0);
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(ShutdownVM.class, a.getClass());

    }


    /**
     * Exhibit issue #43
     */
    @Test
    public void testMultipleStates() {
        Model mo = new DefaultModel();
        VM vm0 = mo.newVM();
        Node n0 = mo.newNode();
        mo.getMapping().ready(vm0).on(n0);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(new HashSet<>(Arrays.asList(vm0)),
                        new HashSet<>(Arrays.asList(vm0)),
                        new HashSet<>(Arrays.asList(vm0)),
                        new HashSet<>(Arrays.asList(vm0))
                ).build();
        Assert.assertNull(rp.solve(2, false));
    }

    @Test
    public void testVMStaySleeping() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        Node n1 = mo.newNode();

        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addSleepingVM(vm1, n1);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(new HashSet<>(),
                        new HashSet<>(),
                        Collections.singleton(vm1),
                        new HashSet<>()).build();

        VMTransition a = rp.getVMActions().get(0);
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(StayAwayVM.class, a.getClass());
    }

    @Test
    public void testVMSleepToRun() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addSleepingVM(vm4, n2);
        map.addReadyVM(vm5);
        map.addReadyVM(vm6);
        Mapping m = mo.getMapping();
        m.addOnlineNode(n1);
        m.addSleepingVM(vm1, n1);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(new HashSet<>(),
                        Collections.singleton(vm1),
                        new HashSet<>(),
                        new HashSet<>()).build();
        VMTransition a = rp.getVMActions().get(0);
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(ResumeVM.class, a.getClass());
    }

    @Test
    public void testNodeOn() throws SchedulerException {
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        Node n1 = mo.newNode();
        m.addOnlineNode(n1);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>()).build();

        NodeTransition a = rp.getNodeActions().get(0);
        Assert.assertEquals(a, rp.getNodeAction(n1));
        Assert.assertEquals(ShutdownableNode.class, a.getClass());
    }


    @Test
    public void testNodeOff() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addSleepingVM(vm4, n2);
        map.addReadyVM(vm5);
        map.addReadyVM(vm6);
        Mapping m = mo.getMapping();
        m.addOfflineNode(n1);

        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>()).build();

        NodeTransition a = rp.getNodeActions().get(rp.getNode(n3));
        Assert.assertEquals(a, rp.getNodeAction(n3));
        Assert.assertEquals(BootableNode.class, a.getClass());
    }

    /**
     * Check the consistency between the variables counting the number of VMs on
     * each node, and the placement variable.
     *
     * @throws org.btrplace.scheduler.SchedulerException
     * @throws ContradictionException
     */
    @Test
    public void testVMCounting() throws SchedulerException, ContradictionException {
        Model mo = new DefaultModel();
        Node n3 = mo.newNode();
        Node n2 = mo.newNode();

        Mapping map = mo.getMapping();
        for (int i = 0; i < 7; i++) {
            VM v = mo.newVM();
            map.addReadyVM(v);
        }
        map.addOnlineNode(n3);
        map.addOnlineNode(n2);
        Parameters ps = new DefaultParameters();
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setParams(ps)
                .setNextVMsStates(new HashSet<>()
                        , map.getAllVMs()
                        , new HashSet<>()
                        , new HashSet<>())
                .build();

        //Restrict the capacity to 5 at most
        for (IntVar capa : rp.getNbRunningVMs()) {
            capa.updateUpperBound(5, Cause.Null);
        }
        new CMinMTTR().inject(ps, rp);
        ReconfigurationPlan p = rp.solve(-1, false);
        Assert.assertNotNull(p);
        //Check consistency between the counting and the hoster variables
        int[] counts = new int[map.getAllNodes().size()];
        for (Node n : map.getOnlineNodes()) {
            int nIdx = rp.getNode(n);
            counts[nIdx] = rp.getNbRunningVMs().get(nIdx).getValue();
        }
        for (VM vm : rp.getFutureRunningVMs()) {
            VMTransition vmo = rp.getVMActions().get(rp.getVM(vm));
            int on = vmo.getDSlice().getHoster().getValue();
            counts[on]--;
        }
        for (int count : counts) {
            Assert.assertEquals(count, 0);
        }
    }

    @Test
    public void testMaintainState() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Mapping map = mo.getMapping();

        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        map.addReadyVM(vm2);
        map.addSleepingVM(vm3, n1);
        map.addReadyVM(vm5);
        ShareableResource rc = new ShareableResource("foo");
        rc.setConsumption(vm1, 5);
        rc.setConsumption(vm2, 7);

        mo.getAttributes().put(vm4, "template", "small");
        mo.attach(rc);

        ReconfigurationProblem rp = new DefaultReconfigurationProblem(mo,
                new DefaultParameters(),
                Collections.singleton(vm4),
                Collections.singleton(vm5),
                Collections.singleton(vm1),
                Collections.emptySet(),
                map.getAllVMs());
        Assert.assertTrue(rp.getFutureSleepingVMs().contains(vm1));
        Assert.assertTrue(rp.getFutureReadyVMs().contains(vm2));
        Assert.assertTrue(rp.getFutureSleepingVMs().contains(vm3));
        Assert.assertTrue(rp.getFutureReadyVMs().contains(vm4));
        Assert.assertTrue(rp.getFutureRunningVMs().contains(vm5));
    }

    /**
     * Test a minimization problem: use the minimum number of nodes.
     *
     * @throws org.btrplace.scheduler.SchedulerException
     */
    @Test
    public void testMinimize() throws SchedulerException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        for (int i = 0; i < 10; i++) {
            Node n = mo.newNode();
            VM vm = mo.newVM();
            map.addOnlineNode(n);
            map.addRunningVM(vm, n);
        }
        Parameters ps = new DefaultParameters();
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).setParams(ps).build();
        Solver s = rp.getSolver();
        IntVar nbNodes = rp.getModel().intVar("nbNodes", 1, map.getAllNodes().size(), true);
        Stream<Slice> dSlices = rp.getVMActions().stream().filter(t -> t.getDSlice() != null).map(VMTransition::getDSlice);
        IntVar[] hosters = dSlices.map(Slice::getHoster).toArray(IntVar[]::new);
        rp.getModel().post(rp.getModel().atMostNValues(hosters, nbNodes, true));

        rp.setObjective(true, nbNodes);
        ReconfigurationPlan plan = rp.solve(-1, true);
        Assert.assertNotNull(plan);
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 1);
        Mapping dst = plan.getResult().getMapping();
        Assert.assertEquals(usedNodes(dst), 1);
    }

    /**
     * Test the report of a timeout.
     */
    @Test(expectedExceptions = {UnstatableProblemException.class})
    public void testTimeout() throws UnstatableProblemException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        for (int i = 0; i < 10; i++) {
            Node n = mo.newNode();
            VM vm = mo.newVM();
            map.addOnlineNode(n);
            map.addRunningVM(vm, n);
        }
        Parameters ps = new DefaultParameters();
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).setParams(ps).build();
        Solver s = rp.getSolver();
        IntVar nbNodes = rp.getModel().intVar("nbNodes", 1, map.getAllNodes().size(), true);
        Stream<Slice> dSlices = rp.getVMActions().stream().filter(t -> t.getDSlice() != null).map(VMTransition::getDSlice);
        IntVar[] hosters = dSlices.map(Slice::getHoster).toArray(IntVar[]::new);
        rp.getModel().post(rp.getModel().atMostNValues(hosters, nbNodes, true));

        rp.setObjective(true, nbNodes);
        rp.getSolver().limitTime(1); // 1 ms
        // -1 will be ignored as it is a negative value (assumed no timeout)
        ReconfigurationPlan plan = rp.solve(-1, true);
        Assert.assertNotNull(plan);
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 1);
        Mapping dst = plan.getResult().getMapping();
        Assert.assertEquals(usedNodes(dst), 1);
    }

    @Test
    public void testStopButton() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        for (int i = 0; i < 10; i++) {
            Node n = mo.newNode();
            VM vm = mo.newVM();
            map.addOnlineNode(n);
            map.addRunningVM(vm, n);
        }
        Parameters ps = new DefaultParameters();
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).setParams(ps).build();
        // We force the stop, so the problem is unstatable despite the solution exists and that
        // there is no timeout.
        rp.stop();
        try {
            rp.solve(0, true);
            Assert.fail("UnstatableProblemException expected");
        } catch (UnstatableProblemException ex) {
            Assert.assertTrue(rp.getSolver().isStopCriterionMet());
        }
    }

    @Test
    public void testViewAddition() throws SchedulerException {
        Model mo = new DefaultModel();
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).build();
        MockCView view = new MockCView();
        Assert.assertTrue(rp.addView(view));
        Assert.assertEquals(rp.getView(view.getIdentifier()), view);
        Assert.assertFalse(rp.addView(view));
    }

    private static int usedNodes(Mapping m) {
        int nb = 0;
        for (Node n : m.getOnlineNodes()) {
            if (!m.getRunningVMs(n).isEmpty()) {
                nb++;
            }
        }
        return nb;
    }
}
