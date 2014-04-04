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

package btrplace.solver.choco;

import btrplace.model.*;
import btrplace.model.view.ModelView;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.actionModel.*;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import btrplace.solver.choco.view.CShareableResource;
import btrplace.solver.choco.view.ChocoModelView;
import btrplace.solver.choco.view.ChocoModelViewBuilder;
import btrplace.solver.choco.view.ModelViewMapper;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.constraints.ICF;
import solver.constraints.IntConstraintFactory;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.VF;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link DefaultReconfigurationProblem}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationProblemTest {

    public class MockCViewModel implements ChocoModelView {
        @Override
        public String getIdentifier() {
            return "cmock";
        }

        @Override
        public boolean beforeSolve(ReconfigurationProblem rp) {
            return true;
        }

        @Override
        public boolean insertActions(ReconfigurationProblem rp, ReconfigurationPlan p) {
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
        public ModelView clone() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean substituteVM(VM curId, VM nextId) {
            throw new UnsupportedOperationException();
        }
    }

    /*private static Model defaultModel() {
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
        return mo;
    }                 */


    /**
     * Just test the state definition of the actions.
     *
     * @throws SolverException should not occur
     */
    @Test
    public void testSimplestInstantiation() throws SolverException {
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
        DurationEvaluators dEval = DurationEvaluators.newBundle();
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(toWait, toRun, Collections.singleton(vm3), Collections.singleton(vm2))
                .setDurationEvaluators(dEval).build();

        Assert.assertEquals(dEval, rp.getDurationEvaluators());
        Assert.assertNotNull(rp.getViewMapper());
        Assert.assertNotNull(rp.getObjectiveAlterer());
        Assert.assertEquals(rp.getFutureReadyVMs(), toWait);
        Assert.assertEquals(rp.getFutureRunningVMs(), toRun);
        Assert.assertEquals(rp.getFutureSleepingVMs(), Collections.singleton(vm3));
        Assert.assertEquals(rp.getFutureKilledVMs(), Collections.singleton(vm2));
        Assert.assertEquals(rp.getVMs().length, 7);
        Assert.assertEquals(rp.getNodes().length, 3);
        Assert.assertEquals(rp.getManageableVMs().size(), rp.getVMs().length, rp.getManageableVMs().toString());
        Assert.assertTrue(rp.getStart().instantiated() && rp.getStart().getValue() == 0);

        //Test the index values of the nodes and the VMs.
        for (int i = 0; i < rp.getVMs().length; i++) {
            VM vm = rp.getVM(i);
            Assert.assertEquals(i, rp.getVM(vm));
        }
        Assert.assertEquals(rp.getVM(mo.newVM()), -1);

        for (int i = 0; i < rp.getNodes().length; i++) {
            Node n = rp.getNode(i);
            Assert.assertEquals(i, rp.getNode(n));
        }
        Assert.assertEquals(rp.getNode(mo.newNode()), -1);
    }

    @Test
    public void testManageableVMs() throws SolverException {
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
                .setNextVMsStates(Collections.<VM>emptySet(), runnings, map.getSleepingVMs(), Collections.<VM>emptySet())
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
    public void testVMToWaiting() throws SolverException {
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
                                new HashSet<VM>(),
                                new HashSet<VM>(),
                                new HashSet<VM>()).build();

        ActionModel a = rp.getVMActions()[rp.getVM(vm1)];
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(ForgeVMModel.class, a.getClass());
    }

    @Test
    public void testWaitinVMToRun() throws SolverException {
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
                .setNextVMsStates(new HashSet<VM>(),
                        Collections.singleton(vm1),
                        new HashSet<VM>(),
                        new HashSet<VM>()).build();

        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(BootVMModel.class, a.getClass());
    }

    @Test
    public void testVMStayRunning() throws SolverException {
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
                .setNextVMsStates(new HashSet<VM>(),
                        Collections.singleton(vm1),
                        new HashSet<VM>(),
                        new HashSet<VM>()).build();
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(RelocatableVMModel.class, a.getClass());
    }

    @Test
    public void testVMRunningToSleeping() throws SolverException {
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
                .setNextVMsStates(new HashSet<VM>(),
                        new HashSet<VM>(),
                        Collections.singleton(vm1),
                        new HashSet<VM>()).build();

        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(SuspendVMModel.class, a.getClass());
    }

    @Test
    public void testVMsToKill() throws SolverException {
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
                .setNextVMsStates(new HashSet<VM>(),
                        new HashSet<VM>(),
                        new HashSet<VM>(),
                        m.getAllVMs()).build();

        for (ActionModel a : rp.getVMActions()) {
            Assert.assertEquals(a.getClass(), KillVMModel.class);
        }
    }

    @Test
    public void testVMToShutdown() throws SolverException {
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
                        new HashSet<VM>(),
                        new HashSet<VM>(),
                        new HashSet<VM>()).build();
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(ShutdownVMModel.class, a.getClass());

    }


    @Test
    public void testVMStaySleeping() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        Node n1 = mo.newNode();

        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addSleepingVM(vm1, n1);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(new HashSet<VM>(),
                        new HashSet<VM>(),
                        Collections.singleton(vm1),
                        new HashSet<VM>()).build();

        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(StayAwayVMModel.class, a.getClass());
    }

    @Test
    public void testVMSleepToRun() throws SolverException {
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
                .setNextVMsStates(new HashSet<VM>(),
                        Collections.singleton(vm1),
                        new HashSet<VM>(),
                        new HashSet<VM>()).build();
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(ResumeVMModel.class, a.getClass());
    }

    @Test
    public void testNodeOn() throws SolverException {
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        Node n1 = mo.newNode();
        m.addOnlineNode(n1);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(new HashSet<VM>(),
                        new HashSet<VM>(),
                        new HashSet<VM>(),
                        new HashSet<VM>()).build();

        ActionModel a = rp.getNodeActions()[0];
        Assert.assertEquals(a, rp.getNodeAction(n1));
        Assert.assertEquals(ShutdownableNodeModel.class, a.getClass());
    }


    @Test
    public void testNodeOff() throws SolverException {
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
                .setNextVMsStates(new HashSet<VM>(),
                        new HashSet<VM>(),
                        new HashSet<VM>(),
                        new HashSet<VM>()).build();

        ActionModel a = rp.getNodeActions()[rp.getNode(n3)];
        Assert.assertEquals(a, rp.getNodeAction(n3));
        Assert.assertEquals(BootableNodeModel.class, a.getClass());
    }

    @Test
    public void testGetResourceMapping() throws SolverException {
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
        ShareableResource rc = new ShareableResource("cpu", 0, 0);
        for (Node n : mo.getMapping().getAllNodes()) {
            rc.setCapacity(n, 4);
        }

        for (VM vm : mo.getMapping().getReadyVMs()) {
            rc.setConsumption(vm, 2);
        }
        mo.attach(rc);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).build();
        CShareableResource rcm = (CShareableResource) rp.getView(ShareableResource.VIEW_ID_BASE + "cpu");
        Assert.assertNotNull(rcm);
        Assert.assertNull(rp.getView("bar"));
        Assert.assertEquals("cpu", rcm.getResourceIdentifier());
        Assert.assertEquals(rc, rcm.getSourceResource());
    }

    @Test
    public void testViewMapping() throws SolverException {
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

        ModelViewMapper mapper = new ModelViewMapper();
        mapper.register(new ChocoModelViewBuilder() {
            @Override
            public Class<? extends ModelView> getKey() {
                return MockView.class;
            }

            @Override
            public ChocoModelView build(ReconfigurationProblem rp, ModelView v) throws SolverException {
                return new MockCViewModel();
            }
        });

        MockView v = new MockView();
        mo.attach(v);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setViewMapper(mapper)
                .build();

        Assert.assertEquals(rp.getViews().size(), 1);
        Assert.assertNotNull(rp.getView("cmock"));
        Assert.assertTrue(rp.getView("cmock") instanceof MockCViewModel);
    }

    @Test
    public void testNoViewImplementation() throws SolverException {
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

        MockView v = new MockView();
        mo.attach(v);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).build();
        Assert.assertNull(rp.getView("mock"));
    }

    /**
     * Check the consistency between the variables counting the number of VMs on
     * each node, and the placement variable.
     *
     * @throws SolverException
     * @throws ContradictionException
     */
    @Test
    public void testVMCounting() throws SolverException, ContradictionException {
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
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(new HashSet<VM>()
                        , map.getAllVMs()
                        , new HashSet<VM>()
                        , new HashSet<VM>())
                .labelVariables()
                .build();

        //Restrict the capacity to 5 at most
        for (IntVar capa : rp.getNbRunningVMs()) {
            capa.updateUpperBound(5, Cause.Null);
        }
        ReconfigurationPlan p = rp.solve(-1, false);
        Assert.assertNotNull(p);
        //Check consistency between the counting and the hoster variables
        int[] counts = new int[map.getAllNodes().size()];
        for (Node n : map.getOnlineNodes()) {
            int nIdx = rp.getNode(n);
            counts[nIdx] = rp.getNbRunningVMs()[nIdx].getValue();
        }
        for (VM vm : rp.getFutureRunningVMs()) {
            VMActionModel vmo = rp.getVMActions()[rp.getVM(vm)];
            int on = vmo.getDSlice().getHoster().getValue();
            counts[on]--;
        }
        for (int count : counts) {
            Assert.assertEquals(count, 0);
        }
    }

    @Test
    public void testMaintainState() throws SolverException {
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

        ActionModelFactory amf = ActionModelFactory.newBundle();
        ReconfigurationProblem rp = new DefaultReconfigurationProblem(mo, DurationEvaluators.newBundle(), new ModelViewMapper(),
                amf,
                Collections.singleton(vm4),
                Collections.singleton(vm5),
                Collections.singleton(vm1),
                Collections.<VM>emptySet(),
                map.getAllVMs(),
                false);
        Assert.assertTrue(rp.getFutureSleepingVMs().contains(vm1));
        Assert.assertTrue(rp.getFutureReadyVMs().contains(vm2));
        Assert.assertTrue(rp.getFutureSleepingVMs().contains(vm3));
        Assert.assertTrue(rp.getFutureReadyVMs().contains(vm4));
        Assert.assertTrue(rp.getFutureRunningVMs().contains(vm5));
    }

    /**
     * Test a minimization problem: use the minimum number of nodes.
     *
     * @throws SolverException
     */
    @Test
    public void testMinimize() throws SolverException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        for (int i = 0; i < 10; i++) {
            Node n = mo.newNode();
            VM vm = mo.newVM();
            map.addOnlineNode(n);
            map.addRunningVM(vm, n);
        }
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        Solver s = rp.getSolver();
        IntVar nbNodes = VF.bounded("nbNodes", 1, map.getAllNodes().size(), s);
        IntVar[] hosters = SliceUtils.extractHoster(ActionModelUtils.getDSlices(rp.getVMActions()));
        s.post(ICF.nvalues(hosters, nbNodes, "at_most_BC"));

        rp.setObjective(true, nbNodes);
        ReconfigurationPlan plan = rp.solve(-1, true);
        Assert.assertNotNull(plan);
        Assert.assertEquals(s.getSearchLoop().getMeasures().getSolutionCount(), 1);
        Mapping dst = plan.getResult().getMapping();
        Assert.assertEquals(usedNodes(dst), 1);
    }

    /**
     * Test a maximization problem: use the maximum number of nodes to host VMs
     *
     * @throws SolverException
     */
    @Test
    public void testMaximization() throws SolverException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        Node n1 = mo.newNode();
        map.addOnlineNode(n1);
        for (int i = 0; i < 10; i++) {
            Node n = mo.newNode();
            VM vm = mo.newVM();
            map.addOnlineNode(n);
            map.addRunningVM(vm, n1);
        }
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        Solver s = rp.getSolver();
        IntVar nbNodes = VF.bounded("nbNodes", 1, map.getOnlineNodes().size(), s);
        IntVar[] hosters = SliceUtils.extractHoster(ActionModelUtils.getDSlices(rp.getVMActions()));
        s.post(IntConstraintFactory.nvalues(hosters, nbNodes, "at_least_AC"));
        rp.setObjective(false, nbNodes);
        ReconfigurationPlan plan = rp.solve(0, true);
        Assert.assertNotNull(plan);
        Mapping dst = plan.getResult().getMapping();
        Assert.assertEquals(s.getSearchLoop().getMeasures().getSolutionCount(), 10);
        Assert.assertEquals(usedNodes(dst), 10);
    }

    /**
     * Test a maximization problem: use the maximum number of nodes to host VMs
     * For a faster optimisation process, the current objective is doubled at each solution
     *
     * @throws SolverException
     */
    @Test
    public void testMaximizationWithAlterer() throws SolverException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        Node n1 = mo.newNode();
        map.addOnlineNode(n1);
        for (int i = 0; i < 10; i++) {
            Node n = mo.newNode();
            VM vm = mo.newVM();
            map.addOnlineNode(n);
            map.addRunningVM(vm, n1);
        }
        final ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        Solver s = rp.getSolver();
        final IntVar nbNodes = VF.bounded("nbNodes", 1, map.getOnlineNodes().size(), s);
        IntVar[] hosters = SliceUtils.extractHoster(ActionModelUtils.getDSlices(rp.getVMActions()));
        s.post(IntConstraintFactory.nvalues(hosters, nbNodes, "at_least_AC"));

        rp.setObjective(false, nbNodes);

        rp.setObjectiveAlterer(new ObjectiveAlterer() {
            @Override
            public int newBound(ReconfigurationProblem rp, int currentValue) {
                return currentValue * 2;
            }
        });

        ReconfigurationPlan plan = rp.solve(0, true);
        Assert.assertNotNull(plan);
        Mapping dst = plan.getResult().getMapping();

        Assert.assertEquals(usedNodes(dst), 8);
        //Note: the optimal value would be 10 but we loose the completeness due to the alterer
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 4);
    }

    /**
     * Test an unsolvable optimisation problem with an alterer. No solution
     *
     * @throws SolverException
     */
    @Test
    public void testUnfeasibleOptimizeWithAlterer() throws SolverException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        for (int i = 0; i < 10; i++) {
            Node n = mo.newNode();
            VM vm = mo.newVM();
            map.addOnlineNode(n);
            map.addRunningVM(vm, n);
        }
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        Solver s = rp.getSolver();
        IntVar nbNodes = VF.bounded("nbNodes", 0, 0, s);
        IntVar[] hosters = SliceUtils.extractHoster(ActionModelUtils.getDSlices(rp.getVMActions()));
        s.post(IntConstraintFactory.nvalues(hosters, nbNodes, "at_most_BC"));
        rp.setObjective(true, nbNodes);
        ObjectiveAlterer alt = new ObjectiveAlterer() {
            @Override
            public int newBound(ReconfigurationProblem rp, int currentValue) {
                return currentValue / 2;
            }
        };

        rp.setObjectiveAlterer(alt);
        ReconfigurationPlan plan = rp.solve(0, true);
        Assert.assertNull(plan);
    }


    @Test
    public void testViewAddition() throws SolverException {
        Model mo = new DefaultModel();
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        MockCViewModel view = new MockCViewModel();
        Assert.assertTrue(rp.addView(view));
        Assert.assertEquals(rp.getView(view.getIdentifier()), view);
        Assert.assertFalse(rp.addView(view));
    }

    private int usedNodes(Mapping m) {
        int nb = 0;
        for (Node n : m.getOnlineNodes()) {
            if (!m.getRunningVMs(n).isEmpty()) {
                nb++;
            }
        }
        return nb;
    }
}
