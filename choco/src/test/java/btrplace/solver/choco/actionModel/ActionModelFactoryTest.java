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
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultReconfigurationProblem;
import btrplace.solver.choco.DefaultReconfigurationProblemBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;

/**
 * Unit tests for {@link btrplace.solver.choco.actionModel.ActionModelFactory}.
 *
 * @author Fabien Hermenier
 */
public class ActionModelFactoryTest {


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
        ActionModelFactory amf = new ActionModelFactory();
        ActionModelFactory.fillWithDefaults(amf);
        System.out.println(amf);
        NodeActionModelBuilder na = amf.getBuilder(m.getState(n1));
        Assert.assertEquals(na.getClass(), ShutdownableNodeModel.Builder.class);
    }

    @Test
    public void testNodeOff() throws SolverException {
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        Node n1 = mo.newNode();
        m.addOfflineNode(n1);
        ActionModelFactory amf = new ActionModelFactory();
        ActionModelFactory.fillWithDefaults(amf);
        NodeActionModelBuilder na = amf.getBuilder(m.getState(n1));
        Assert.assertEquals(na.getClass(), BootableNodeModel.Builder.class);
    }
}
