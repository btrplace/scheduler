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
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.ShutdownNode;
import btrplace.plan.event.ShutdownVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultReconfigurationProblemBuilder;
import btrplace.solver.choco.DurationEvaluators;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import choco.kernel.solver.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.UUID;

/**
 * Unit tests for {@link ShutdownableNodeModel}.
 *
 * @author Fabien hermenier
 */
public class ShutdownableNodeModelTest {

    @Test
    public void testBasics() throws SolverException {
        UUID n1 = UUID.randomUUID();
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        Model mo = new DefaultModel(map);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .labelVariables()
                .build();
        ShutdownableNodeModel ma = (ShutdownableNodeModel) rp.getNodeAction(n1);
        Assert.assertEquals(ma.getNode(), n1);
        Assert.assertEquals(ma.getHostingStart(), rp.getStart());
    }

    @Test
    public void testForcedOnline() throws SolverException, ContradictionException {
        UUID n1 = UUID.randomUUID();
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(ShutdownNode.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .labelVariables()
                .build();
        ShutdownableNodeModel ma = (ShutdownableNodeModel) rp.getNodeAction(n1);
        ma.getState().setVal(1);
        Assert.assertEquals(rp.solve(0, true), Boolean.TRUE);
        Assert.assertEquals(ma.getDuration().getVal(), 0);
        Assert.assertEquals(ma.getStart().getVal(), 0);
        Assert.assertEquals(ma.getEnd().getVal(), 0);
        Assert.assertEquals(ma.getHostingStart().getVal(), 0);
        Assert.assertEquals(ma.getHostingEnd().getVal(), 0);
        ReconfigurationPlan p = rp.extractSolution();
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 0);
        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().getOnlineNodes().contains(n1));
    }

    @Test
    public void testForcedOffline() throws SolverException, ContradictionException {
        UUID n1 = UUID.randomUUID();
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(ShutdownNode.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setDurationEvaluatators(dev)
                .labelVariables()
                .build();
        ShutdownableNodeModel ma = (ShutdownableNodeModel) rp.getNodeAction(n1);
        ma.getState().setVal(0);
        Assert.assertEquals(rp.solve(0, true), Boolean.TRUE);
        Assert.assertEquals(ma.getDuration().getVal(), 5);
        Assert.assertEquals(ma.getStart().getVal(), 0);
        Assert.assertEquals(ma.getEnd().getVal(), 5);
        Assert.assertEquals(ma.getHostingStart().getVal(), 0);
        Assert.assertEquals(ma.getHostingEnd().getVal(), 0);
        ReconfigurationPlan p = rp.extractSolution();
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 1);
        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n1));
    }

    @Test
    public void testScheduledShutdown() throws SolverException, ContradictionException {
        UUID n1 = UUID.randomUUID();
        UUID vm1 = UUID.randomUUID();
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(ShutdownVM.class, new ConstantDuration(2));
        dev.register(ShutdownNode.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setDurationEvaluatators(dev)
                .labelVariables()
                .setNextVMsStates(Collections.singleton(vm1), Collections.<UUID>emptySet(), Collections.<UUID>emptySet(), Collections.<UUID>emptySet())
                .build();
        ShutdownableNodeModel ma = (ShutdownableNodeModel) rp.getNodeAction(n1);
        ma.getState().setVal(0);
        Assert.assertEquals(rp.solve(0, true), Boolean.TRUE);
        Assert.assertEquals(ma.getDuration().getVal(), 5);
        Assert.assertEquals(ma.getStart().getVal(), 2);
        Assert.assertEquals(ma.getEnd().getVal(), 7);
        Assert.assertEquals(ma.getHostingStart().getVal(), 0);
        Assert.assertEquals(ma.getHostingEnd().getVal(), 2);
        ReconfigurationPlan p = rp.extractSolution();
        Assert.assertNotNull(p);
        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n1));


    }
}
