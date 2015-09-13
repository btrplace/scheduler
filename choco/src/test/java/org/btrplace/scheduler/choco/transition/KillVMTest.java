/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.DefaultReconfigurationProblemBuilder;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.duration.ConstantActionDuration;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;

import java.util.HashSet;
import java.util.Set;


/**
 * Unit tests for {@link KillVM}.
 *
 * @author Fabien Hermenier
 */
public class KillVMTest {

    /**
     * Test the action model with different action models.
     *
     * @throws ContradictionException
     * @throws org.btrplace.scheduler.SchedulerException
     */
    @Test
    public void testBasics() throws ContradictionException, SchedulerException {
        Model mo = new DefaultModel();

        Mapping map = mo.getMapping();

        Node n1 = mo.newNode();
        map.addOnlineNode(n1);
        VM vm1 = mo.newVM();
        map.addRunningVM(vm1, n1);

        VM vm2 = mo.newVM();
        map.addReadyVM(vm2);
        VM vm3 = mo.newVM();
        map.addSleepingVM(vm3, n1);

        Set<VM> empty = new HashSet<>();
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(org.btrplace.plan.event.KillVM.class, new ConstantActionDuration(1));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(empty, empty, empty, map.getAllVMs())
                .build();

        rp.getNodeAction(n1).getState().instantiateTo(rp.getVM(vm1), Cause.Null);
        //Common stuff
        for (VM vm : map.getAllVMs()) {
            KillVM m = (KillVM) rp.getVMAction(vm);
            Assert.assertEquals(vm, m.getVM());
            Assert.assertTrue(m.getState().isInstantiatedTo(0));
            Assert.assertNull(m.getDSlice());
            Assert.assertTrue(m.getDuration().isInstantiatedTo(1));
            Assert.assertTrue(m.getStart().isInstantiatedTo(0));
            Assert.assertTrue(m.getEnd().isInstantiatedTo(1));
        }

        //The waiting and the sleeping VM have no CSlice
        Assert.assertNull(rp.getVMAction(vm2).getCSlice());
        Assert.assertNull(rp.getVMAction(vm3).getCSlice());

        //The running VM has a CSlice
        Assert.assertNotNull(rp.getVMAction(vm1).getCSlice());
        System.out.println(rp.getVMAction(vm1).getCSlice() + " " + rp.getNode(n1));
        Assert.assertTrue(rp.getVMAction(vm1).getCSlice().getHoster().isInstantiatedTo(rp.getNode(n1)));
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);

        for (Action a : p) {
            if (a instanceof org.btrplace.plan.event.KillVM) {
                org.btrplace.plan.event.KillVM vma = (org.btrplace.plan.event.KillVM) a;
                Assert.assertEquals(1, a.getEnd());
                Assert.assertEquals(0, a.getStart());
                if (vma.getVM().equals(vm1) || vma.getVM().equals(vm3)) {
                    Assert.assertEquals(vma.getNode(), n1);
                } else if (vma.getVM().equals(vm2)) {
                    Assert.assertNull(vma.getNode());
                } else {
                    Assert.fail();
                }
            }
        }
    }

}
