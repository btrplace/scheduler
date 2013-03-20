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

import btrplace.model.*;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.Preserve;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.MigrateVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.*;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import btrplace.test.PremadeElements;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link RelocatableVMModel}.
 *
 * @author Fabien Hermenier
 */
public class RelocatableVMModelTest implements PremadeElements {

    @Test
    public void testForcedToMove() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);

        DurationEvaluators dev = new DurationEvaluators();
        dev.register(MigrateVM.class, new ConstantDuration(5));
        Model mo = new DefaultModel(map);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(Collections.<UUID>emptySet(), map.getAllVMs(), Collections.<UUID>emptySet(), Collections.<UUID>emptySet())
                .setDurationEvaluatators(dev)
                .labelVariables()
                .build();
        rp.getNodeActions()[0].getState().setVal(1);
        rp.getNodeActions()[1].getState().setVal(1);
        RelocatableVMModel am = (RelocatableVMModel) rp.getVMAction(vm1);
        Assert.assertEquals(vm1, am.getVM());
        Assert.assertEquals(2, am.getDuration().getDomainSize());
        Assert.assertEquals(0, am.getDuration().getInf());
        Assert.assertEquals(5, am.getDuration().getSup());
        Assert.assertFalse(am.getStart().isInstantiated());
        Assert.assertFalse(am.getEnd().isInstantiated());
        Assert.assertNotNull(am.getCSlice());
        Assert.assertTrue(am.getCSlice().getHoster().isInstantiatedTo(rp.getNode(n1)));
        Assert.assertTrue(am.getState().isInstantiatedTo(1));
        Assert.assertNotNull(am.getDSlice());
        Assert.assertFalse(am.getDSlice().getHoster().isInstantiated());

        //No VMs on n1, discrete mode
        CPSolver s = rp.getSolver();
        s.post(s.eq(rp.getNbRunningVMs()[rp.getNode(n1)], 0));

        ReconfigurationPlan p = rp.solve(0, false);

        Assert.assertNotNull(p);
        Model m = p.getResult();
        Assert.assertEquals(n2, m.getMapping().getVMLocation(vm1));

        MigrateVM a = (MigrateVM) p.getActions().iterator().next();
        Assert.assertEquals(0, a.getStart());
        Assert.assertEquals(5, a.getEnd());
        Assert.assertEquals(n1, a.getSourceNode());
        Assert.assertEquals(n2, a.getDestinationNode());
        Assert.assertEquals(vm1, a.getVM());
    }

    @Test
    public void testForcedToStay() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);

        DurationEvaluators dev = new DurationEvaluators();
        dev.register(MigrateVM.class, new ConstantDuration(5));
        Model mo = new DefaultModel(map);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(Collections.<UUID>emptySet(), map.getAllVMs(), Collections.<UUID>emptySet(), Collections.<UUID>emptySet())
                .setDurationEvaluatators(dev)
                .build();
        rp.getNodeActions()[0].getState().setVal(1);
        rp.getNodeActions()[1].getState().setVal(1);
        RelocatableVMModel am = (RelocatableVMModel) rp.getVMAction(vm1);

        //No VMs on n2
        rp.getNbRunningVMs()[rp.getNode(n2)].setVal(0);

        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        Assert.assertEquals(0, p.getSize());

        Assert.assertTrue(am.getDuration().isInstantiatedTo(0));
        Assert.assertTrue(am.getDSlice().getHoster().isInstantiatedTo(rp.getNode(n1)));
        Assert.assertTrue(am.getStart().isInstantiatedTo(0));
        Assert.assertTrue(am.getEnd().isInstantiatedTo(0));


        Model m = p.getResult();
        Assert.assertEquals(n1, m.getMapping().getVMLocation(vm1));
    }

    @Test
    public void testRelocateDueToPreserve() throws SolverException {
        Mapping map = new DefaultMapping();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 10);
        rc.set(n1, 7);
        rc.set(vm1, 3);
        rc.set(vm2, 3);
        rc.set(vm3, 5);

        Preserve pr = new Preserve(map.getAllVMs(), "cpu", 5);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        Model mo = new DefaultModel(map);
        mo.attach(rc);
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(new Online(map.getAllNodes()));
        cstrs.add(new Overbook(map.getAllNodes(), "cpu", 1));
        cstrs.add(pr);
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
    }
}
