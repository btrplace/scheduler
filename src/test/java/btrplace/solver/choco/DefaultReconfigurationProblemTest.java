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
import btrplace.plan.SolverException;
import btrplace.solver.choco.actionModel.*;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.ContradictionException;
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


    private static Model defaultModel() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(nOn1);
        map.addOnlineNode(nOn2);
        map.addOfflineNode(nOff);

        map.setVMRunOn(vm1, nOn1);
        map.setVMRunOn(vm2, nOn1);
        map.setVMRunOn(vm3, nOn2);
        map.setVMSleepOn(vm4, nOn2);
        map.addWaitingVM(vm5);
        map.addWaitingVM(vm6);
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
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblem(m,
                dEval,
                toWait, //wait
                toRun,                      //run
                Collections.singleton(vm3), //sleep
                Collections.singleton(vm2)  //destroy

        );

        Assert.assertEquals(dEval, rp.getDurationEvaluators());
        Assert.assertEquals(rp.getFutureWaitingVMs(), toWait);
        Assert.assertEquals(rp.getFutureRunningVMs(), toRun);
        Assert.assertEquals(rp.getFutureSleepingVMs(), Collections.singleton(vm3));
        Assert.assertEquals(rp.getFutureDestroyedVMs(), Collections.singleton(vm2));
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
    public void testShortInstantiation() throws SolverException {
        Model m = defaultModel();
        DurationEvaluators d = new DurationEvaluators();
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblem(m, d);
        Assert.assertEquals(d, rp.getDurationEvaluators());
        Assert.assertTrue(rp.getFutureDestroyedVMs().isEmpty());
        Assert.assertEquals(m.getMapping().getRunningVMs(), rp.getFutureRunningVMs());
        Assert.assertEquals(m.getMapping().getSleepingVMs(), rp.getFutureSleepingVMs());
        Assert.assertEquals(m.getMapping().getWaitingVMs(), rp.getFutureWaitingVMs());
    }

    @Test
    public void testVMToWaiting() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblem(new DefaultModel(m),
                Collections.singleton(vm),
                new HashSet<UUID>(),
                new HashSet<UUID>(),
                new HashSet<UUID>());
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMActions()[0]);
        Assert.assertEquals(InstantiateVMModel.class, a.getClass());
    }

    @Test
    public void testWaitinVMToRun() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        m.addWaitingVM(vm);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblem(new DefaultModel(m),
                new HashSet<UUID>(),
                Collections.singleton(vm),
                new HashSet<UUID>(),
                new HashSet<UUID>());
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMActions()[0]);
        Assert.assertEquals(BootVMModel.class, a.getClass());
    }

    @Test
    public void testVMStayRunning() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        UUID n = UUID.randomUUID();
        m.addOnlineNode(n);
        m.setVMRunOn(vm, n);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblem(new DefaultModel(m),
                new HashSet<UUID>(),
                Collections.singleton(vm),
                new HashSet<UUID>(),
                new HashSet<UUID>());
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMActions()[0]);
        Assert.assertEquals(RelocatableVMModel.class, a.getClass());
    }

    @Test
    public void testVMRunningToSleeping() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        UUID n = UUID.randomUUID();
        m.addOnlineNode(n);
        m.setVMRunOn(vm, n);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblem(new DefaultModel(m),
                new HashSet<UUID>(),
                new HashSet<UUID>(),
                Collections.singleton(vm),
                new HashSet<UUID>());
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMActions()[0]);
        Assert.assertEquals(SuspendVMModel.class, a.getClass());
    }

    @Test
    public void testVMRunningToTerminated() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        UUID n = UUID.randomUUID();
        m.addOnlineNode(n);
        m.setVMRunOn(vm, n);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblem(new DefaultModel(m),
                new HashSet<UUID>(),
                new HashSet<UUID>(),
                new HashSet<UUID>(),
                Collections.singleton(vm)
        );
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(a, rp.getVMActions()[0]);
        Assert.assertEquals(ShutdownVMModel.class, a.getClass());
    }

    @Test
    public void testVMStaySleeping() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        UUID n = UUID.randomUUID();
        m.addOnlineNode(n);
        m.setVMSleepOn(vm, n);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblem(new DefaultModel(m),
                new HashSet<UUID>(),
                new HashSet<UUID>(),
                Collections.singleton(vm),
                new HashSet<UUID>()
        );
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(StayAwayVMModel.class, a.getClass());
    }

    @Test
    public void testVMSleepToRun() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        UUID n = UUID.randomUUID();
        m.addOnlineNode(n);
        m.setVMSleepOn(vm, n);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblem(new DefaultModel(m),
                new HashSet<UUID>(),
                Collections.singleton(vm),
                new HashSet<UUID>(),
                new HashSet<UUID>()
        );
        ActionModel a = rp.getVMActions()[0];
        Assert.assertEquals(ResumeVMModel.class, a.getClass());
    }

    @Test
    public void testNodeOn() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID n = UUID.randomUUID();
        m.addOnlineNode(n);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblem(new DefaultModel(m),
                new HashSet<UUID>(),
                new HashSet<UUID>(),
                new HashSet<UUID>(),
                new HashSet<UUID>()
        );
        ActionModel a = rp.getNodeActions()[0];
        Assert.assertEquals(ShutdownableNodeModel.class, a.getClass());
    }

    @Test
    public void testNodeOff() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID n = UUID.randomUUID();
        m.addOfflineNode(n);
        DefaultReconfigurationProblem rp = new DefaultReconfigurationProblem(new DefaultModel(m),
                new HashSet<UUID>(),
                new HashSet<UUID>(),
                new HashSet<UUID>(),
                new HashSet<UUID>()
        );
        ActionModel a = rp.getNodeActions()[0];
        Assert.assertEquals(BootableNodeModel.class, a.getClass());
    }

    @Test
    public void testGetResourceMapping() throws SolverException {
        Model m = defaultModel();
        IntResource rc = new DefaultIntResource("cpu", 0);
        for (UUID n : m.getMapping().getAllNodes()) {
            rc.set(n, 4);
        }

        for (UUID vm : m.getMapping().getWaitingVMs()) {
            rc.set(vm, 2);
        }
        m.attach(rc);
        ReconfigurationProblem rp = new DefaultReconfigurationProblem(m);
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
            map.addWaitingVM(vm);
        }
        map.removeNode(nOff);
        m = new DefaultModel(map);
        ReconfigurationProblem rp = new DefaultReconfigurationProblem(m
                , new HashSet<UUID>()
                , map.getAllVMs()
                , new HashSet<UUID>()
                , new HashSet<UUID>());

        rp.labelVariables(true);
        Assert.assertEquals(Boolean.TRUE, rp.getSolver().solve());
        //Restrict the capacity to 2 at most
        try {
            for (IntDomainVar capa : rp.getVMsCountOnNodes()) {
                capa.setSup(5);
            }
        } catch (ContradictionException e) {
            Assert.fail(e.getMessage(), e);
        }

        //Check consistency between the counting and the hoster variables
        int[] counts = new int[map.getAllNodes().size()];
        for (UUID n : map.getOnlineNodes()) {
            int nIdx = rp.getNode(n);
            counts[nIdx] = rp.getVMsCountOnNodes()[nIdx].getVal();
        }
        System.out.println(Arrays.toString(counts));
        for (UUID vm : rp.getFutureRunningVMs()) {
            ActionModel mo = rp.getVMActions()[rp.getVM(vm)];
            int on = mo.getDSlice().getHoster().getInf();
            System.out.println(mo + " " + on);
            counts[on]--;
        }
        for (int i = 0; i < counts.length; i++) {
            Assert.assertEquals(0, counts[i]);
        }
        ChocoLogging.flushLogs();
    }
}
