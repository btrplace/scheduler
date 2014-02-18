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
import btrplace.plan.event.Action;
import btrplace.plan.event.KillVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultReconfigurationProblemBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.durationEvaluator.ConstantActionDuration;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.exception.ContradictionException;

import java.util.HashSet;
import java.util.Set;


/**
 * Unit tests for {@link KillVMActionModel}.
 *
 * @author Fabien Hermenier
 */
public class KillVMActionModelTest {

    /**
     * Test the action model with different action models.
     *
     * @throws ContradictionException
     * @throws SolverException
     */
    @Test
    public void testBasics() throws ContradictionException, SolverException {
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
        dev.register(KillVM.class, new ConstantActionDuration(1));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables()
                .setNextVMsStates(empty, empty, empty, map.getAllVMs())
                .build();

        rp.getNodeAction(n1).getState().instantiateTo(rp.getVM(vm1), Cause.Null);
        //Common stuff
        for (VM vm : map.getAllVMs()) {
            KillVMActionModel m = (KillVMActionModel) rp.getVMAction(vm);
            Assert.assertEquals(vm, m.getVM());
            Assert.assertTrue(m.getState().instantiatedTo(0));
            Assert.assertNull(m.getDSlice());
            Assert.assertTrue(m.getDuration().instantiatedTo(1));
            Assert.assertTrue(m.getStart().instantiatedTo(0));
            Assert.assertTrue(m.getEnd().instantiatedTo(1));
        }

        //The waiting and the sleeping VM have no CSlice
        Assert.assertNull(rp.getVMAction(vm2).getCSlice());
        Assert.assertNull(rp.getVMAction(vm3).getCSlice());

        //The running VM has a CSlice
        Assert.assertNotNull(rp.getVMAction(vm1).getCSlice());
        System.out.println(rp.getVMAction(vm1).getCSlice() + " " + rp.getNode(n1));
        Assert.assertTrue(rp.getVMAction(vm1).getCSlice().getHoster().instantiatedTo(rp.getNode(n1)));
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);

        for (Action a : p) {
            if (a instanceof KillVM) {
                KillVM vma = (KillVM) a;
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
