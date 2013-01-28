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
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.actionModel.*;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
        ShareableResource rc = new DefaultShareableResource("cpu", 0);
        for (UUID n : m.getMapping().getAllNodes()) {
            rc.set(n, 4);
        }

        for (UUID vm : m.getMapping().getReadyVMs()) {
            rc.set(vm, 2);
        }
        m.attach(rc);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(m).build();
        ResourceMapping rcm = rp.getResourceMapping("cpu");
        Assert.assertNotNull(rcm);
        Assert.assertNull(rp.getResourceMapping("bar"));
        Assert.assertEquals("cpu", rcm.getIdentifier());
        Assert.assertEquals(rc, rcm.getSourceResource());
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
        ShareableResource rc = new DefaultShareableResource("foo");
        rc.set(vm1, 5);
        rc.set(vm2, 7);

        Model mo = new DefaultModel(map);
        mo.attach(rc);

        ReconfigurationProblem rp = new DefaultReconfigurationProblem(mo, new DurationEvaluators(),
                map.getReadyVMs(),
                map.getRunningVMs(),
                map.getSleepingVMs(),
                Collections.<UUID>emptySet(),
                map.getAllVMs(),
                false);
        ReconfigurationPlan p = rp.solve(0, true);

        //Check the amount of allocated resources on the RP
        ResourceMapping rcm = rp.getResourceMapping("foo");
        Assert.assertEquals(rcm.getVMsAllocation()[rp.getVM(vm1)].getVal(), 5);
        Assert.assertEquals(rcm.getVMsAllocation()[rp.getVM(vm2)].getVal(), 7);

        //And on the resulting plan.
        Model res = p.getResult();
        Assert.assertEquals(res.getResource("foo").get(vm1), 5);
        Assert.assertEquals(res.getResource("foo").get(vm2), 7);
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
        ShareableResource rc = new DefaultShareableResource("foo");
        rc.set(v1, 5);
        rc.set(v2, 7);

        Model mo = new DefaultModel(map);
        mo.attach(rc);

        ReconfigurationProblem rp = new DefaultReconfigurationProblem(mo, new DurationEvaluators(),
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
}
