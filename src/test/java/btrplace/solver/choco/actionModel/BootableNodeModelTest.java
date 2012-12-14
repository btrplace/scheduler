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
import btrplace.plan.event.BootNode;
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
 * Unit tests for {@link BootableNodeModel}.
 *
 * @author Fabien Hermenier
 */
public class BootableNodeModelTest {

    @Test
    public void testBasic() throws SolverException {
        UUID n1 = UUID.randomUUID();
        Mapping map = new DefaultMapping();
        map.addOfflineNode(n1);

        Model mo = new DefaultModel(map);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).build();
        BootableNodeModel na = (BootableNodeModel) rp.getNodeAction(n1);
        Assert.assertEquals(na.getNode(), n1);
        Assert.assertTrue(na.getStart().isInstantiatedTo(0));

    }

    @Test
    public void testForcingBoot() throws SolverException, ContradictionException {
        UUID n1 = UUID.randomUUID();
        Mapping map = new DefaultMapping();
        map.addOfflineNode(n1);

        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(BootNode.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .labelVariables()
                .setDurationEvaluatators(dev)
                .build();
        BootableNodeModel na = (BootableNodeModel) rp.getNodeAction(n1);
        na.getState().setVal(1);
        ReconfigurationPlan p = rp.solve(0, true);
        Assert.assertEquals(na.getDuration().getVal(), 5);
        Assert.assertEquals(na.getStart().getVal(), 0);
        Assert.assertEquals(na.getEnd().getVal(), 5);
        Assert.assertEquals(na.getHostingStart().getVal(), 5);
        Assert.assertEquals(na.getHostingEnd().getVal(), 5);

        Assert.assertNotNull(p);
        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().getOnlineNodes().contains(n1));
    }

    @Test
    public void testForcingOffline() throws SolverException, ContradictionException {
        UUID n1 = UUID.randomUUID();
        Mapping map = new DefaultMapping();
        map.addOfflineNode(n1);

        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(BootNode.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .labelVariables()
                .setDurationEvaluatators(dev)
                .build();
        BootableNodeModel na = (BootableNodeModel) rp.getNodeAction(n1);
        na.getState().setVal(0);
        ReconfigurationPlan p = rp.solve(0, true);
        Assert.assertEquals(na.getDuration().getVal(), 0);
        Assert.assertEquals(na.getStart().getVal(), 0);
        Assert.assertEquals(na.getEnd().getVal(), 0);
        Assert.assertEquals(na.getHostingStart().getVal(), rp.getEnd().getVal());
        Assert.assertEquals(na.getHostingEnd().getVal(), rp.getEnd().getVal());

        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 0);
        Model res = p.getResult();
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n1));
    }

    @Test
    public void testRequiredOnline() throws SolverException, ContradictionException {
        UUID n1 = UUID.randomUUID();
        UUID vm1 = UUID.randomUUID();
        Mapping map = new DefaultMapping();
        map.addOfflineNode(n1);
        map.addReadyVM(vm1);

        Model mo = new DefaultModel(map);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(BootNode.class, new ConstantDuration(5));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(Collections.<UUID>emptySet(), Collections.singleton(vm1), Collections.<UUID>emptySet(), Collections.<UUID>emptySet())
                .labelVariables()
                .setDurationEvaluatators(dev)
                .build();
        Assert.assertNotNull(rp.solve(0, true));
    }

}
