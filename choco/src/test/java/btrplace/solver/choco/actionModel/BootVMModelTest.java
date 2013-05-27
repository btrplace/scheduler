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

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.plan.event.BootVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultReconfigurationProblemBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import btrplace.test.PremadeElements;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Iterator;


/**
 * Basic unit tests for {@link BootVMModel}.
 *
 * @author Fabien Hermenier
 */
public class BootVMModelTest implements PremadeElements {

    /**
     * Just boot a VM on a  node.
     */
    @Test
    public void testBasics() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addReadyVM(vm1);

        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(BootVM.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setDurationEvaluatators(dev)
                .labelVariables()
                .setNextVMsStates(new HashSet<Integer>(), map.getAllVMs(), new HashSet<Integer>(), new HashSet<Integer>())
                .build();
        rp.getNodeActions()[0].getState().setVal(1);
        rp.getNodeActions()[1].getState().setVal(1);
        BootVMModel m = (BootVMModel) rp.getVMActions()[0];
        Assert.assertEquals(vm1, m.getVM());
        Assert.assertNull(m.getCSlice());
        Assert.assertTrue(m.getDuration().isInstantiatedTo(5));
        Assert.assertTrue(m.getState().isInstantiatedTo(1));
        Assert.assertFalse(m.getDSlice().getHoster().isInstantiated());
        Assert.assertFalse(m.getDSlice().getStart().isInstantiated());
        Assert.assertFalse(m.getDSlice().getEnd().isInstantiated());

        ReconfigurationPlan p = rp.solve(0, false);
        BootVM a = (BootVM) p.getActions().iterator().next();

        int dest = rp.getNode(m.getDSlice().getHoster().getVal());
        Assert.assertEquals(vm1, a.getVM());
        Assert.assertEquals(dest, a.getDestinationNode());
        Assert.assertEquals(5, a.getEnd() - a.getStart());
    }

    /**
     * Test that check when the action is shorter than the end of
     * the reconfiguration process.
     * In practice, 2 boot actions have to be executed sequentially
     */
    @Test
    public void testBootSequence() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addReadyVM(vm1);
        map.addReadyVM(vm2);

        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(BootVM.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setDurationEvaluatators(dev)
                .labelVariables()
                .setNextVMsStates(new HashSet<Integer>(), map.getAllVMs(), new HashSet<Integer>(), new HashSet<Integer>())
                .build();
        BootVMModel m1 = (BootVMModel) rp.getVMActions()[rp.getVMIdx(vm1)];
        BootVMModel m2 = (BootVMModel) rp.getVMActions()[rp.getVMIdx(vm2)];
        rp.getNodeActions()[0].getState().setVal(1);
        rp.getNodeActions()[1].getState().setVal(1);
        CPSolver s = rp.getSolver();
        s.post(s.geq(m2.getStart(), m1.getEnd()));

        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        Iterator<Action> ite = p.iterator();
        BootVM b1 = (BootVM) ite.next();
        BootVM b2 = (BootVM) ite.next();
        Assert.assertEquals(vm1, b1.getVM());
        Assert.assertEquals(vm2, b2.getVM());
        Assert.assertTrue(b1.getEnd() <= b2.getStart());
        Assert.assertEquals(5, b1.getEnd() - b1.getStart());
        Assert.assertEquals(5, b2.getEnd() - b2.getStart());

    }
}
