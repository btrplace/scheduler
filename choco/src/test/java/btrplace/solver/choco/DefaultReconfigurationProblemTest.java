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

package btrplace.solver.choco;

import btrplace.model.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.actionModel.*;
import btrplace.solver.choco.view.CShareableResource;
import btrplace.test.PremadeElements;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.AtMostNValue;
import choco.cp.solver.constraints.global.IncreasingNValue;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.ResolutionPolicy;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link DefaultReconfigurationProblem}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationProblemTest implements PremadeElements {

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
    }

    private static Model defaultModel() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addSleepingVM(vm4, n2);
        map.addReadyVM(vm5);
        map.addReadyVM(vm6);
        return new DefaultModel(map);
    }


    /**
     * Just test the state definition of the actions.
     *
     * @throws SolverException should not occur
     */
    @Test
    public void testSimplestInstantiation() throws SolverException {
        Model m = defaultModel();
        Set<UUID> toRun = new HashSet<UUID>();
        Set<UUID> toWait = new HashSet<UUID>();
        toWait.add(vm6);
        toWait.add(vm7);
        toRun.add(vm5);
        toRun.add(vm4);
        toRun.add(vm1);
        m.getAttributes().put(vm7, "template", "small");
        DurationEvaluators dEval = new DurationEvaluators();
        UUIDPool p = new InMemoryUUIDPool();
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(m)
                .setUUIDPool(p)
                .setNextVMsStates(toWait, toRun, Collections.singleton(vm3), Collections.singleton(vm2))
                .setDurationEvaluatators(dEval).build();

        Assert.assertEquals(dEval, rp.getDurationEvaluators());
        Assert.assertNotNull(rp.getViewMapper());
        Assert.assertNull(rp.getObjectiveAlterer());
        Assert.assertEquals(rp.getUUIDPool(), p);
        Assert.assertEquals(rp.getFutureReadyVMs(), toWait);
        Assert.assertEquals(rp.getFutureRunningVMs(), toRun);
        Assert.assertEquals(rp.getFutureSleepingVMs(), Collections.singleton(vm3));
        Assert.assertEquals(rp.getFutureKilledVMs(), Collections.singleton(vm2));
        Assert.assertEquals(rp.getVMs().length, 7);
        Assert.assertEquals(rp.getNodes().length, 3);
        Assert.assertEquals(rp.getManageableVMs().size(), rp.getVMs().length);
        Assert.assertTrue(rp.getStart().isInstantiated() && rp.getStart().getVal() == 0);

        //Test the index values of the nodes and the VMs.
        for (int i = 0; i < rp.getVMs().length; i++) {
            UUID vm = rp.getVM(i);
            Assert.assertEquals(i, rp.getVM(vm));
        }
        Assert.assertEquals(-1, rp.getVM(vm10));

        for (int i = 0; i < rp.getNodes().length; i++) {
            UUID n = rp.getNode(i);
            Assert.assertEquals(i, rp.getNode(n));
        }
        Assert.assertEquals(-1, rp.getNode(n10));
    }

    @Test
    public void testManageableVMs() throws SolverException {
        Model mo = defaultModel();
        Mapping map = mo.getMapping();
        Set<UUID> runnings = new HashSet<UUID>(map.getRunningVMs());
        runnings.add(vm6);
        runnings.add(vm5);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(Collections.<UUID>emptySet(), runnings, map.getSleepingVMs(), Collections.<UUID>emptySet())
                .setManageableVMs(map.getRunningVMs(n1)).build();
        Set<UUID> manageable = rp.getManageableVMs();

        Assert.assertEquals(manageable.size(), 4);
        Assert.assertTrue(manageable.containsAll(Arrays.asList(vm6, vm5, vm1, vm2)));
        //Check the action model that has been used for each of the VM.
        for (UUID vm : map.getAllVMs()) {
            if (map.getRunningVMs().contains(vm) && rp.getFutureRunningVMs().contains(vm)) {
                if (!manageable.contains(vm)) {
                    Assert.assertEquals(rp.getVMAction(vm).getClass(), StayRunningVMModel.class);
                } else {
                    Assert.assertEquals(rp.getVMAction(vm).getClass(), RelocatableVMModel.class);
                }
            } else {
                Assert.assertNotEquals(rp.getVMAction(vm).getClass(), StayRunningVMModel.class);
            }
        }
    }


    @Test
    public void testVMToWaiting() throws SolverException {
        Mapping m = new DefaultMapping();
        Model mo = new DefaultModel(m);
        mo.getAttributes().put(vm1, "template", "small");
        ReconfigurationProblem rp =
                new DefaultReconfigurationProblemBuilder(mo)
                        .setNextVMsStates(Collections.singleton(vm1),
                                new HashSet<UUID>(),
                                new HashSet<UUID>(),
                                new HashSet<UUID>()).build();

        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(ForgeVMModel.class, a.getClass());
    }

    @Test
    public void testWaitinVMToRun() throws SolverException {
        Mapping m = new DefaultMapping();
        m.addReadyVM(vm1);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        Collections.singleton(vm1),
                        new HashSet<UUID>(),
                        new HashSet<UUID>()).build();

        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(BootVMModel.class, a.getClass());
    }

    @Test
    public void testVMStayRunning() throws SolverException {
        Mapping m = new DefaultMapping();
        m.addOnlineNode(n1);
        m.addRunningVM(vm1, n1);

        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        Collections.singleton(vm1),
                        new HashSet<UUID>(),
                        new HashSet<UUID>()).build();
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(RelocatableVMModel.class, a.getClass());
    }

    @Test
    public void testVMRunningToSleeping() throws SolverException {
        Mapping m = new DefaultMapping();
        m.addOnlineNode(n1);
        m.addRunningVM(vm1, n1);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        Collections.singleton(vm1),
                        new HashSet<UUID>()).build();

        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(SuspendVMModel.class, a.getClass());
    }

    @Test
    public void testVMsToKill() throws SolverException {
        Mapping m = new DefaultMapping();
        m.addOnlineNode(n1);
        m.addRunningVM(vm1, n1);
        m.addSleepingVM(vm2, n1);
        m.addReadyVM(vm3);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        m.getAllVMs()).build();

        for (ActionModel a : rp.getVMActions()) {
            Assert.assertEquals(a.getClass(), KillVMActionModel.class);
        }
    }

    @Test
    public void testVMToShutdown() throws SolverException {
        Mapping m = new DefaultMapping();
        m.addOnlineNode(n1);
        m.addRunningVM(vm1, n1);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(Collections.singleton(vm1),
                        new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        new HashSet<UUID>()).build();
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(ShutdownVMModel.class, a.getClass());

    }


    @Test
    public void testVMStaySleeping() throws SolverException {
        Mapping m = new DefaultMapping();
        m.addOnlineNode(n1);
        m.addSleepingVM(vm1, n1);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        Collections.singleton(vm1),
                        new HashSet<UUID>()).build();

        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(StayAwayVMModel.class, a.getClass());
    }

    @Test
    public void testVMSleepToRun() throws SolverException {
        Mapping m = new DefaultMapping();
        m.addOnlineNode(n1);
        m.addSleepingVM(vm1, n1);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        Collections.singleton(vm1),
                        new HashSet<UUID>(),
                        new HashSet<UUID>()).build();
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm1));
        Assert.assertEquals(ResumeVMModel.class, a.getClass());
    }

    @Test
    public void testNodeOn() throws SolverException {
        Mapping m = new DefaultMapping();
        m.addOnlineNode(n1);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        new HashSet<UUID>()).build();

        ActionModel a = rp.getNodeActions()[0];
        Assert.assertEquals(a, rp.getNodeAction(n1));
        Assert.assertEquals(ShutdownableNodeModel.class, a.getClass());
    }


    @Test
    public void testNodeOff() throws SolverException {
        Mapping m = new DefaultMapping();
        m.addOfflineNode(n1);

        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        new HashSet<UUID>()).build();

        ActionModel a = rp.getNodeActions()[0];
        Assert.assertEquals(a, rp.getNodeAction(n1));
        Assert.assertEquals(BootableNodeModel.class, a.getClass());
    }

    @Test
    public void testGetResourceMapping() throws SolverException {
        Model m = defaultModel();
        ShareableResource rc = new ShareableResource("cpu", 0);
        for (UUID n : m.getMapping().getAllNodes()) {
            rc.set(n, 4);
        }

        for (UUID vm : m.getMapping().getReadyVMs()) {
            rc.set(vm, 2);
        }
        m.attach(rc);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(m).build();
        CShareableResource rcm = (CShareableResource) rp.getView(ShareableResource.VIEW_ID_BASE + "cpu");
        Assert.assertNotNull(rcm);
        Assert.assertNull(rp.getView("bar"));
        Assert.assertEquals("cpu", rcm.getResourceIdentifier());
        Assert.assertEquals(rc, rcm.getSourceResource());
    }

    @Test
    public void testViewMapping() throws SolverException {
        Model m = defaultModel();

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
        m.attach(v);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(m)
                .setViewMapper(mapper)
                .build();

        Assert.assertEquals(rp.getViews().size(), 1);
        Assert.assertNotNull(rp.getView("cmock"));
        Assert.assertTrue(rp.getView("cmock") instanceof MockCViewModel);
    }

    @Test(expectedExceptions = {SolverException.class})
    public void testNoViewImplementation() throws SolverException {
        Model m = defaultModel();

        MockView v = new MockView();
        m.attach(v);

        new DefaultReconfigurationProblemBuilder(m).build();
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
        Model m = defaultModel();
        Mapping map = m.getMapping().clone();
        Set<UUID> s = new HashSet<UUID>(map.getAllVMs());
        for (UUID vm : s) {
            map.addReadyVM(vm);
        }
        map.removeNode(n3);
        m = new DefaultModel(map);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(m)
                .setNextVMsStates(new HashSet<UUID>()
                        , map.getAllVMs()
                        , new HashSet<UUID>()
                        , new HashSet<UUID>())
                .labelVariables()
                .build();

        for (IntDomainVar capa : rp.getNbRunningVMs()) {
            capa.setSup(5);
        }
        //Restrict the capacity to 2 at most
        ReconfigurationPlan p = rp.solve(-1, false);
        Assert.assertNotNull(p);
        //Check consistency between the counting and the hoster variables
        int[] counts = new int[map.getAllNodes().size()];
        for (UUID n : map.getOnlineNodes()) {
            int nIdx = rp.getNode(n);
            counts[nIdx] = rp.getNbRunningVMs()[nIdx].getVal();
        }
        for (UUID vm : rp.getFutureRunningVMs()) {
            VMActionModel mo = rp.getVMActions()[rp.getVM(vm)];
            int on = mo.getDSlice().getHoster().getVal();
            counts[on]--;
        }
        for (int i = 0; i < counts.length; i++) {
            Assert.assertEquals(counts[i], 0);
        }
    }

    @Test
    public void testMaintainState() throws SolverException {
        Mapping map = new DefaultMapping();

        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        map.addReadyVM(vm2);
        map.addSleepingVM(vm3, n1);
        map.addReadyVM(vm5);
        ShareableResource rc = new ShareableResource("foo");
        rc.set(vm1, 5);
        rc.set(vm2, 7);

        Model mo = new DefaultModel(map);
        mo.getAttributes().put(vm4, "template", "small");
        mo.attach(rc);

        ReconfigurationProblem rp = new DefaultReconfigurationProblem(mo, new DurationEvaluators(), new ModelViewMapper(),
                new InMemoryUUIDPool(),
                Collections.singleton(vm4),
                Collections.singleton(vm5),
                Collections.singleton(vm1),
                Collections.<UUID>emptySet(),
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
        Mapping map = new DefaultMapping();
        for (int i = 0; i < 10; i++) {
            UUID n = new UUID(2, i);
            UUID vm = new UUID(3, i);
            map.addOnlineNode(n);
            map.addRunningVM(vm, n);
        }
        Model mo = new DefaultModel(map);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        CPSolver s = rp.getSolver();
        IntDomainVar nbNodes = s.createBoundIntVar("nbNodes", 1, map.getAllNodes().size());
        IntDomainVar[] hosters = SliceUtils.extractHosters(ActionModelUtils.getDSlices(rp.getVMActions()));
        s.post(new AtMostNValue(hosters, nbNodes));

        s.setObjective(nbNodes);
        s.getConfiguration().putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MINIMIZE);
        ReconfigurationPlan plan = rp.solve(0, true);
        Assert.assertNotNull(plan);
        Assert.assertEquals(s.getNbSolutions(), 10);
        Mapping dst = plan.getResult().getMapping();
        Assert.assertEquals(MappingUtils.usedNodes(dst, EnumSet.of(MappingUtils.State.Runnings)).size(), 1);
    }

    /**
     * Test a minimization problem: use the minimum number of nodes. For a faster reduction,
     * an alterer divide the current objective by 2 at each solution
     *
     * @throws SolverException
     */
    @Test
    public void testMinimizationWithAlterer() throws SolverException {
        Mapping map = new DefaultMapping();
        for (int i = 0; i < 10; i++) {
            UUID n = new UUID(2, i);
            UUID vm = new UUID(3, i);
            map.addOnlineNode(n);
            map.addRunningVM(vm, n);
        }
        Model mo = new DefaultModel(map);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        CPSolver s = rp.getSolver();
        IntDomainVar nbNodes = s.createBoundIntVar("nbNodes", 1, map.getAllNodes().size());
        IntDomainVar[] hosters = SliceUtils.extractHosters(ActionModelUtils.getDSlices(rp.getVMActions()));
        s.post(new AtMostNValue(hosters, nbNodes));
        s.setObjective(nbNodes);
        s.getConfiguration().putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MINIMIZE);

        ObjectiveAlterer alt = new ObjectiveAlterer(rp) {
            @Override
            public int tryNewValue(int currentValue) {
                return currentValue / 2;
            }
        };

        rp.setObjectiveAlterer(alt);
        Assert.assertEquals(rp.getObjectiveAlterer(), alt);
        ReconfigurationPlan plan = rp.solve(0, true);
        Assert.assertNotNull(plan);
        Assert.assertEquals(s.getNbSolutions(), 4);
        Mapping dst = plan.getResult().getMapping();
        Assert.assertEquals(MappingUtils.usedNodes(dst, EnumSet.of(MappingUtils.State.Runnings)).size(), 1);
    }

    /**
     * Test a maximization problem: use the maximum number of nodes to host VMs
     *
     * @throws SolverException
     */
    @Test
    public void testMaximization() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        for (int i = 0; i < 10; i++) {
            UUID n = new UUID(2, i);
            UUID vm = new UUID(3, i);
            map.addOnlineNode(n);
            map.addRunningVM(vm, n1);
        }
        Model mo = new DefaultModel(map);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        CPSolver s = rp.getSolver();
        IntDomainVar nbNodes = s.createBoundIntVar("nbNodes", 1, map.getOnlineNodes().size());
        IntDomainVar[] hosters = SliceUtils.extractHosters(ActionModelUtils.getDSlices(rp.getVMActions()));
        s.post(new IncreasingNValue(nbNodes, hosters, IncreasingNValue.Mode.ATLEAST));
        s.setObjective(nbNodes);
        s.getConfiguration().putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MAXIMIZE);

        ReconfigurationPlan plan = rp.solve(0, true);
        Assert.assertNotNull(plan);
        Mapping dst = plan.getResult().getMapping();
        Assert.assertEquals(s.getNbSolutions(), 10);
        Assert.assertEquals(MappingUtils.usedNodes(dst, EnumSet.of(MappingUtils.State.Runnings)).size(), 10);
    }

    /**
     * Test a maximization problem: use the maximum number of nodes to host VMs
     * For a faster optimisation process, the current objective is doubled at each solution
     *
     * @throws SolverException
     */
    @Test
    public void testMaximizationWithAlterer() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        for (int i = 0; i < 10; i++) {
            UUID n = new UUID(2, i);
            UUID vm = new UUID(3, i);
            map.addOnlineNode(n);
            map.addRunningVM(vm, n1);
        }
        Model mo = new DefaultModel(map);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        CPSolver s = rp.getSolver();
        final IntDomainVar nbNodes = s.createBoundIntVar("nbNodes", 1, map.getOnlineNodes().size());
        IntDomainVar[] hosters = SliceUtils.extractHosters(ActionModelUtils.getDSlices(rp.getVMActions()));
        s.post(new IncreasingNValue(nbNodes, hosters, IncreasingNValue.Mode.ATLEAST));
        s.setObjective(nbNodes);
        s.getConfiguration().putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MAXIMIZE);

        ObjectiveAlterer alt = new ObjectiveAlterer(rp) {
            @Override
            public int tryNewValue(int currentValue) {
                return currentValue * 2;
            }
        };

        rp.setObjectiveAlterer(alt);

        ReconfigurationPlan plan = rp.solve(0, true);
        Assert.assertNotNull(plan);
        Mapping dst = plan.getResult().getMapping();
        Assert.assertEquals(MappingUtils.usedNodes(dst, EnumSet.of(MappingUtils.State.Runnings)).size(), 8);
        //Note: the optimal value would be 10 but we loose the completeness due to the alterer
        Assert.assertEquals(s.getNbSolutions(), 4);

    }

    /**
     * Test an unsolvable optimisation problem with an alterer. No solution
     *
     * @throws SolverException
     */
    @Test
    public void testUnfeasibleOptimizeWithAlterer() throws SolverException {
        Mapping map = new DefaultMapping();
        for (int i = 0; i < 10; i++) {
            UUID n = new UUID(2, i);
            UUID vm = new UUID(3, i);
            map.addOnlineNode(n);
            map.addRunningVM(vm, n);
        }
        Model mo = new DefaultModel(map);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        CPSolver s = rp.getSolver();
        IntDomainVar nbNodes = s.createBoundIntVar("nbNodes", 0, 0);
        IntDomainVar[] hosters = SliceUtils.extractHosters(ActionModelUtils.getDSlices(rp.getVMActions()));
        s.post(new AtMostNValue(hosters, nbNodes));
        s.setObjective(nbNodes);
        s.getConfiguration().putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MINIMIZE);

        ObjectiveAlterer alt = new ObjectiveAlterer(rp) {
            @Override
            public int tryNewValue(int currentValue) {
                return currentValue / 2;
            }
        };

        rp.setObjectiveAlterer(alt);
        ReconfigurationPlan plan = rp.solve(0, true);
        Assert.assertNull(plan);
    }
}
