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
public class DefaultReconfigurationProblemTest {

    private static UUID nOn1 = UUID.randomUUID();
    private static UUID nOn2 = UUID.randomUUID();
    private static UUID nOff = UUID.randomUUID();

    private static UUID vm1 = UUID.randomUUID();
    private static UUID vm2 = UUID.randomUUID();
    private static UUID vm3 = UUID.randomUUID();
    private static UUID vm4 = UUID.randomUUID();
    private static UUID vm5 = UUID.randomUUID();
    private static UUID vm6 = UUID.randomUUID();
    private static UUID vm7 = UUID.randomUUID();

    public class MockCViewModel implements ChocoModelView {
        @Override
        public String getIdentifier() {
            return "cmock";
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
        map.addOnlineNode(nOn1);
        map.addOnlineNode(nOn2);
        map.addOfflineNode(nOff);

        map.addRunningVM(vm1, nOn1);
        map.addRunningVM(vm2, nOn1);
        map.addRunningVM(vm3, nOn2);
        map.addSleepingVM(vm4, nOn2);
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
        DurationEvaluators dEval = new DurationEvaluators();
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(m)
                .setNextVMsStates(toWait, toRun, Collections.singleton(vm3), Collections.singleton(vm2))
                .setDurationEvaluatators(dEval).build();

        Assert.assertEquals(dEval, rp.getDurationEvaluators());
        Assert.assertNotNull(rp.getViewMapper());
        Assert.assertEquals(rp.getFutureReadyVMs(), toWait);
        Assert.assertEquals(rp.getFutureRunningVMs(), toRun);
        Assert.assertEquals(rp.getFutureSleepingVMs(), Collections.singleton(vm3));
        Assert.assertEquals(rp.getFutureKilledVMs(), Collections.singleton(vm2));
        Assert.assertEquals(rp.getVMs().length, 7);
        Assert.assertEquals(rp.getNodes().length, 3);

        Assert.assertTrue(rp.getStart().isInstantiated() && rp.getStart().getVal() == 0);

        //Test the index values of the nodes and the VMs.
        for (int i = 0; i < rp.getVMs().length; i++) {
            UUID vm = rp.getVM(i);
            Assert.assertEquals(i, rp.getVM(vm));
        }
        Assert.assertEquals(-1, rp.getVM(UUID.randomUUID()));

        for (int i = 0; i < rp.getNodes().length; i++) {
            UUID n = rp.getNode(i);
            Assert.assertEquals(i, rp.getNode(n));
        }
        Assert.assertEquals(-1, rp.getNode(UUID.randomUUID()));
    }

    @Test
    public void testVMToWaiting() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        ReconfigurationProblem rp =
                new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                        .setNextVMsStates(Collections.singleton(vm),
                                new HashSet<UUID>(),
                                new HashSet<UUID>(),
                                new HashSet<UUID>()).build();

        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm));
        Assert.assertEquals(ForgeVMModel.class, a.getClass());
    }

    @Test
    public void testWaitinVMToRun() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        m.addReadyVM(vm);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        Collections.singleton(vm),
                        new HashSet<UUID>(),
                        new HashSet<UUID>()).build();

        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm));
        Assert.assertEquals(BootVMModel.class, a.getClass());
    }

    @Test
    public void testVMStayRunning() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        UUID n = UUID.randomUUID();
        m.addOnlineNode(n);
        m.addRunningVM(vm, n);

        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        Collections.singleton(vm),
                        new HashSet<UUID>(),
                        new HashSet<UUID>()).build();
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm));
        Assert.assertEquals(RelocatableVMModel.class, a.getClass());
    }

    @Test
    public void testVMRunningToSleeping() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        UUID n = UUID.randomUUID();
        m.addOnlineNode(n);
        m.addRunningVM(vm, n);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        Collections.singleton(vm),
                        new HashSet<UUID>()).build();

        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm));
        Assert.assertEquals(SuspendVMModel.class, a.getClass());
    }

    @Test
    public void testVMsToKill() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        UUID n = UUID.randomUUID();
        m.addOnlineNode(n);
        m.addRunningVM(UUID.randomUUID(), n);
        m.addSleepingVM(UUID.randomUUID(), n);
        m.addReadyVM(UUID.randomUUID());
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
        UUID vm = UUID.randomUUID();
        UUID n = UUID.randomUUID();
        m.addOnlineNode(n);
        m.addRunningVM(vm, n);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(Collections.singleton(vm),
                        new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        new HashSet<UUID>()).build();
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm));
        Assert.assertEquals(ShutdownVMModel.class, a.getClass());

    }


    @Test
    public void testVMStaySleeping() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        UUID n = UUID.randomUUID();
        m.addOnlineNode(n);
        m.addSleepingVM(vm, n);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        Collections.singleton(vm),
                        new HashSet<UUID>()).build();

        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm));
        Assert.assertEquals(StayAwayVMModel.class, a.getClass());
    }

    @Test
    public void testVMSleepToRun() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        UUID n = UUID.randomUUID();
        m.addOnlineNode(n);
        m.addSleepingVM(vm, n);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        Collections.singleton(vm),
                        new HashSet<UUID>(),
                        new HashSet<UUID>()).build();
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMAction(vm));
        Assert.assertEquals(ResumeVMModel.class, a.getClass());
    }

    @Test
    public void testNodeOn() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID n = UUID.randomUUID();
        m.addOnlineNode(n);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        new HashSet<UUID>()).build();

        ActionModel a = rp.getNodeActions()[0];
        Assert.assertEquals(a, rp.getNodeAction(n));
        Assert.assertEquals(ShutdownableNodeModel.class, a.getClass());
    }


    @Test
    public void testNodeOff() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID n = UUID.randomUUID();
        m.addOfflineNode(n);

        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel(m))
                .setNextVMsStates(new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        new HashSet<UUID>(),
                        new HashSet<UUID>()).build();

        ActionModel a = rp.getNodeActions()[0];
        Assert.assertEquals(a, rp.getNodeAction(n));
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

        Assert.assertNotNull(rp.getView("cmock"));
        Assert.assertTrue(rp.getView("cmock") instanceof MockCViewModel);
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
        map.removeNode(nOff);
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
        Assert.assertEquals(Boolean.TRUE, rp.getSolver().solve());

        //Check consistency between the counting and the hoster variables
        int[] counts = new int[map.getAllNodes().size()];
        for (UUID n : map.getOnlineNodes()) {
            int nIdx = rp.getNode(n);
            counts[nIdx] = rp.getNbRunningVMs()[nIdx].getVal();
        }
        for (UUID vm : rp.getFutureRunningVMs()) {
            VMActionModel mo = rp.getVMActions()[rp.getVM(vm)];
            int on = mo.getDSlice().getHoster().getInf();
            counts[on]--;
        }
        for (int i = 0; i < counts.length; i++) {
            Assert.assertEquals(0, counts[i]);
        }
    }

    @Test
    public void testMaintainResourceUsage() throws SolverException {
        Mapping map = new DefaultMapping();

        UUID n1 = UUID.randomUUID();
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();

        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        ShareableResource rc = new ShareableResource("foo");
        rc.set(vm1, 5);
        rc.set(vm2, 7);

        Model mo = new DefaultModel(map);
        mo.attach(rc);

        ReconfigurationProblem rp = new DefaultReconfigurationProblem(mo, new DurationEvaluators(), new ModelViewMapper(),
                map.getReadyVMs(),
                map.getRunningVMs(),
                map.getSleepingVMs(),
                Collections.<UUID>emptySet(),
                map.getAllVMs(),
                false);
        ReconfigurationPlan p = rp.solve(0, false);

        //Check the amount of allocated resources on the RP
        CShareableResource rcm = (CShareableResource) rp.getView(btrplace.model.view.ShareableResource.VIEW_ID_BASE + "foo");
        Assert.assertEquals(rcm.getVMsAllocation()[rp.getVM(vm1)].getVal(), 5);
        Assert.assertEquals(rcm.getVMsAllocation()[rp.getVM(vm2)].getVal(), 7);

        //And on the resulting plan.
        Model res = p.getResult();
        Assert.assertEquals(((ShareableResource) res.getView(btrplace.model.view.ShareableResource.VIEW_ID_BASE + "foo")).get(vm1), 5);
        Assert.assertEquals(((ShareableResource) res.getView(btrplace.model.view.ShareableResource.VIEW_ID_BASE + "foo")).get(vm2), 7);
    }

    @Test
    public void testMaintainState() throws SolverException {
        Mapping map = new DefaultMapping();

        UUID n1 = UUID.randomUUID();
        UUID v1 = UUID.randomUUID();
        UUID v2 = UUID.randomUUID();
        UUID v3 = UUID.randomUUID();
        UUID v4 = UUID.randomUUID();
        UUID v5 = UUID.randomUUID();
        map.addOnlineNode(n1);
        map.addRunningVM(v1, n1);
        map.addReadyVM(v2);
        map.addSleepingVM(v3, n1);
        map.addReadyVM(v5);
        ShareableResource rc = new ShareableResource("foo");
        rc.set(v1, 5);
        rc.set(v2, 7);

        Model mo = new DefaultModel(map);
        mo.attach(rc);

        ReconfigurationProblem rp = new DefaultReconfigurationProblem(mo, new DurationEvaluators(), new ModelViewMapper(),
                Collections.singleton(v4),
                Collections.singleton(v5),
                Collections.singleton(v1),
                Collections.<UUID>emptySet(),
                map.getAllVMs(),
                false);
        Assert.assertTrue(rp.getFutureSleepingVMs().contains(v1));
        Assert.assertTrue(rp.getFutureReadyVMs().contains(v2));
        Assert.assertTrue(rp.getFutureSleepingVMs().contains(v3));
        Assert.assertTrue(rp.getFutureReadyVMs().contains(v4));
        Assert.assertTrue(rp.getFutureRunningVMs().contains(v5));
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
            UUID n = UUID.randomUUID();
            UUID vm = UUID.randomUUID();
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
            UUID n = UUID.randomUUID();
            UUID vm = UUID.randomUUID();
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
        UUID n1 = UUID.randomUUID();
        map.addOnlineNode(n1);
        for (int i = 0; i < 10; i++) {
            UUID n = UUID.randomUUID();
            UUID vm = UUID.randomUUID();
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
        UUID n1 = UUID.randomUUID();
        map.addOnlineNode(n1);
        for (int i = 0; i < 10; i++) {
            UUID n = UUID.randomUUID();
            UUID vm = UUID.randomUUID();
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
            UUID n = UUID.randomUUID();
            UUID vm = UUID.randomUUID();
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
