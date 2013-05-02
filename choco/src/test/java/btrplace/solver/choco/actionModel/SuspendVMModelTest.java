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
import btrplace.plan.event.Action;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.SuspendVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultReconfigurationProblemBuilder;
import btrplace.solver.choco.DurationEvaluators;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import btrplace.test.PremadeElements;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

/**
 * Unit tests for {@link SuspendVMModel}.
 *
 * @author Fabien Hermenier
 */
public class SuspendVMModelTest implements PremadeElements {

    @Test
    public void testBasic() throws ContradictionException, SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);

        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(SuspendVM.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setDurationEvaluatators(dev)
                .labelVariables()
                .setNextVMsStates(new HashSet<UUID>(), new HashSet<UUID>(), map.getAllVMs(), new HashSet<UUID>())
                .build();
        rp.getNodeActions()[0].getState().setVal(1);
        SuspendVMModel m = (SuspendVMModel) rp.getVMActions()[0];
        Assert.assertEquals(vm1, m.getVM());
        Assert.assertNull(m.getDSlice());
        Assert.assertTrue(m.getDuration().isInstantiatedTo(5));
        Assert.assertTrue(m.getState().isInstantiatedTo(0));
        Assert.assertTrue(m.getCSlice().getHoster().isInstantiatedTo(0));

        ReconfigurationPlan p = rp.solve(0, false);
        SuspendVM a = (SuspendVM) p.getActions().iterator().next();
        Assert.assertEquals(n1, a.getSourceNode());
        Assert.assertEquals(vm1, a.getVM());
        Assert.assertEquals(5, a.getEnd() - a.getStart());
    }

    /**
     * Test that check that the action duration is lesser than
     * the cSlice duration. This allows actions scheduling
     * In practice, for this test, 2 suspend actions have to be executed sequentially
     */
    @Test
    public void testSuspendSequences() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);

        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(SuspendVM.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setDurationEvaluatators(dev)
                .labelVariables()
                .setNextVMsStates(new HashSet<UUID>(), new HashSet<UUID>(), map.getAllVMs(), new HashSet<UUID>())
                .build();
        SuspendVMModel m1 = (SuspendVMModel) rp.getVMActions()[rp.getVM(vm1)];
        SuspendVMModel m2 = (SuspendVMModel) rp.getVMActions()[rp.getVM(vm2)];
        rp.getNodeActions()[0].getState().setVal(1);
        CPSolver s = rp.getSolver();
        s.post(s.geq(m2.getStart(), m1.getEnd()));

        ReconfigurationPlan p = rp.solve(0, false);

        Assert.assertNotNull(p);
        Iterator<Action> ite = p.iterator();
        SuspendVM b1 = (SuspendVM) ite.next();
        SuspendVM b2 = (SuspendVM) ite.next();
        Assert.assertEquals(vm1, b1.getVM());
        Assert.assertEquals(vm2, b2.getVM());
        Assert.assertTrue(b1.getEnd() <= b2.getStart());
        Assert.assertEquals(5, b1.getEnd() - b1.getStart());
        Assert.assertEquals(5, b2.getEnd() - b2.getStart());

    }
}
