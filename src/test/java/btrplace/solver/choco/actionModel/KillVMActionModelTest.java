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
import btrplace.plan.Action;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.KillVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultReconfigurationProblemBuilder;
import btrplace.solver.choco.DurationEvaluators;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import choco.kernel.solver.ContradictionException;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
        Mapping map = new DefaultMapping();

        UUID n1 = UUID.randomUUID();
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();

        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        map.addReadyVM(vm2);
        map.addSleepingVM(vm3, n1);

        Model mo = new DefaultModel(map);
        Set<UUID> empty = new HashSet<UUID>();
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(KillVM.class, new ConstantDuration(1));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables()
                .setNextVMsStates(empty, empty, empty, map.getAllVMs())
                .build();

        rp.getNodeAction(n1).getState().setVal(1);
        //Common stuff
        for (UUID vm : map.getAllVMs()) {
            KillVMActionModel m = (KillVMActionModel) rp.getVMAction(vm);
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
        Assert.assertTrue(rp.getVMAction(vm1).getCSlice().getHoster().isInstantiatedTo(rp.getNode(n1)));
        ReconfigurationPlan p = rp.solve(0, true);
        Assert.assertNotNull(p);

        for (Action a : p) {
            KillVM vma = (KillVM) a;
            Assert.assertEquals(1, a.getEnd());
            Assert.assertEquals(0, a.getStart());
            if (vma.getVM().equals(vm1) || vma.getVM().equals(vm3)) {
                Assert.assertEquals(n1, vma.getNode());
            } else if (vma.getVM().equals(vm2)) {
                Assert.assertNull(vma.getNode());
            } else {
                Assert.fail();
            }
        }
    }

}
