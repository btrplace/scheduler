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
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.Preserve;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.ForgeVM;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultReconfigurationProblemBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import btrplace.solver.choco.objective.minMTTR.MinMTTR;
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
        Assert.assertTrue(am.getRelocationMethod().isInstantiatedTo(0));
        Assert.assertEquals(vm1, am.getVM());
        Assert.assertEquals(2, am.getDuration().getDomainSize());
        Assert.assertEquals(0, am.getDuration().getInf());
        Assert.assertEquals(5, am.getDuration().getSup());
        Assert.assertFalse(am.getStart().isInstantiated());
        Assert.assertFalse(am.getEnd().isInstantiated());
        Assert.assertNotNull(am.getCSlice());
        Assert.assertTrue(am.getCSlice().getHoster().isInstantiatedTo(rp.getNodeIdx(n1)));
        Assert.assertTrue(am.getState().isInstantiatedTo(1));
        Assert.assertNotNull(am.getDSlice());
        Assert.assertFalse(am.getDSlice().getHoster().isInstantiated());

        //No VMs on n1, discrete mode
        CPSolver s = rp.getSolver();
        s.post(s.eq(rp.getNbRunningVMs()[rp.getNodeIdx(n1)], 0));

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
        rp.getNbRunningVMs()[rp.getNodeIdx(n2)].setVal(0);

        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        Assert.assertEquals(0, p.getSize());

        Assert.assertTrue(am.getDuration().isInstantiatedTo(0));
        Assert.assertTrue(am.getDSlice().getHoster().isInstantiatedTo(rp.getNodeIdx(n1)));
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
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Online(map.getAllNodes()));
        cstrs.add(new Overbook(map.getAllNodes(), "cpu", 1));
        cstrs.add(pr);
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
    }

    /**
     * The re-instantiation is possible but will lead in a waste of time.
     *
     * @throws SolverException
     * @throws ContradictionException
     */
    @Test
    public void testNotWorthyReInstantiation() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(MigrateVM.class, new ConstantDuration(2));
        Model mo = new DefaultModel(map);

        mo.getAttributes().put(vm1, "template", "small");
        mo.getAttributes().put(vm1, "clone", true);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(Collections.<UUID>emptySet(), map.getAllVMs(), Collections.<UUID>emptySet(), Collections.<UUID>emptySet())
                .setDurationEvaluatators(dev)
                .labelVariables()
                .build();
        RelocatableVMModel am = (RelocatableVMModel) rp.getVMAction(vm1);
        Assert.assertFalse(am.getRelocationMethod().isInstantiated());
    }

    /**
     * The re-instantiation is possible and worthy.
     *
     * @throws SolverException
     * @throws ContradictionException
     */
    @Test
    public void testWorthyReInstantiation() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm10, n1); //Not using vm1 because UUIDPool starts at 0 so their will be multiple (0,1) VMs.
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(MigrateVM.class, new ConstantDuration(20));
        dev.register(ForgeVM.class, new ConstantDuration(3));
        dev.register(BootVM.class, new ConstantDuration(2));
        dev.register(ShutdownVM.class, new ConstantDuration(1));
        Model mo = new DefaultModel(map);

        mo.getAttributes().put(vm10, "template", "small");
        mo.getAttributes().put(vm10, "clone", true);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(Collections.<UUID>emptySet(), map.getAllVMs(), Collections.<UUID>emptySet(), Collections.<UUID>emptySet())
                .setDurationEvaluatators(dev)
                .labelVariables()
                .setManageableVMs(map.getAllVMs())
                .build();
        RelocatableVMModel am = (RelocatableVMModel) rp.getVMAction(vm10);
        am.getDSlice().getHoster().setVal(rp.getNodeIdx(n2));
        new MinMTTR().inject(rp);
        ReconfigurationPlan p = rp.solve(10, true);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertTrue(am.getRelocationMethod().isInstantiatedTo(1));
        Assert.assertEquals(p.getSize(), 3);
        Model res = p.getResult();
        //Check the VM has been relocated
        Assert.assertEquals(res.getMapping().getRunningVMs(n1).size(), 0);
        Assert.assertEquals(res.getMapping().getRunningVMs(n2).size(), 1);
        Assert.assertNotNull(p);
    }

    /**
     * The re-instantiation is possible and worthy.
     *
     * @throws SolverException
     * @throws ContradictionException
     */
    @Test
    public void testWorthlessReInstantiation() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm10, n1); //Not using vm1 because UUIDPool starts at 0 so their will be multiple (0,1) VMs.
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(MigrateVM.class, new ConstantDuration(2));
        dev.register(ForgeVM.class, new ConstantDuration(3));
        dev.register(BootVM.class, new ConstantDuration(2));
        dev.register(ShutdownVM.class, new ConstantDuration(1));
        Model mo = new DefaultModel(map);

        mo.getAttributes().put(vm10, "template", "small");
        mo.getAttributes().put(vm10, "clone", true);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(Collections.<UUID>emptySet(), map.getAllVMs(), Collections.<UUID>emptySet(), Collections.<UUID>emptySet())
                .setDurationEvaluatators(dev)
                .labelVariables()
                .setManageableVMs(map.getAllVMs())
                .build();
        RelocatableVMModel am = (RelocatableVMModel) rp.getVMAction(vm10);
        am.getDSlice().getHoster().setVal(rp.getNodeIdx(n2));
        new MinMTTR().inject(rp);
        ReconfigurationPlan p = rp.solve(10, true);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertTrue(am.getRelocationMethod().isInstantiatedTo(0));
        Assert.assertEquals(p.getSize(), 1);
        Model res = p.getResult();
        //Check the VM has been relocated
        Assert.assertEquals(res.getMapping().getRunningVMs(n1).size(), 0);
        Assert.assertEquals(res.getMapping().getRunningVMs(n2).size(), 1);
        Assert.assertNotNull(p);
    }

    @Test
    public void testForcedReInstantiation() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm10, n1); //Not using vm1 because UUIDPool starts at 0 so their will be multiple (0,1) VMs.
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(MigrateVM.class, new ConstantDuration(20));
        dev.register(ForgeVM.class, new ConstantDuration(3));
        dev.register(BootVM.class, new ConstantDuration(2));
        dev.register(ShutdownVM.class, new ConstantDuration(1));
        Model mo = new DefaultModel(map);

        mo.getAttributes().put(vm10, "template", "small");
        mo.getAttributes().put(vm10, "clone", true);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(Collections.<UUID>emptySet(), map.getAllVMs(), Collections.<UUID>emptySet(), Collections.<UUID>emptySet())
                .setDurationEvaluatators(dev)
                .labelVariables()
                .setManageableVMs(map.getAllVMs())
                .build();
        RelocatableVMModel am = (RelocatableVMModel) rp.getVMAction(vm10);
        am.getRelocationMethod().setVal(1);
        am.getDSlice().getHoster().setVal(rp.getNodeIdx(n2));
        new MinMTTR().inject(rp);
        ReconfigurationPlan p = rp.solve(10, true);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertTrue(am.getRelocationMethod().isInstantiatedTo(1));
        Assert.assertEquals(p.getSize(), 3);
        Model res = p.getResult();
        //Check the VM has been relocated
        Assert.assertEquals(res.getMapping().getRunningVMs(n1).size(), 0);
        Assert.assertEquals(res.getMapping().getRunningVMs(n2).size(), 1);
        Assert.assertNotNull(p);
    }

    @Test
    public void testForcedMigration() throws SolverException, ContradictionException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm10, n1); //Not using vm1 because UUIDPool starts at 0 so their will be multiple (0,1) VMs.
        DurationEvaluators dev = new DurationEvaluators();
        dev.register(MigrateVM.class, new ConstantDuration(20));
        dev.register(ForgeVM.class, new ConstantDuration(3));
        dev.register(BootVM.class, new ConstantDuration(2));
        dev.register(ShutdownVM.class, new ConstantDuration(1));
        Model mo = new DefaultModel(map);

        mo.getAttributes().put(vm10, "template", "small");
        mo.getAttributes().put(vm10, "clone", true);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(Collections.<UUID>emptySet(), map.getAllVMs(), Collections.<UUID>emptySet(), Collections.<UUID>emptySet())
                .setDurationEvaluatators(dev)
                .labelVariables()
                .setManageableVMs(map.getAllVMs())
                .build();
        RelocatableVMModel am = (RelocatableVMModel) rp.getVMAction(vm10);
        am.getRelocationMethod().setVal(0);
        am.getDSlice().getHoster().setVal(rp.getNodeIdx(n2));
        new MinMTTR().inject(rp);
        ReconfigurationPlan p = rp.solve(10, true);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertTrue(am.getRelocationMethod().isInstantiatedTo(0));
        Assert.assertEquals(p.getSize(), 1);
        Model res = p.getResult();
        //Check the VM has been relocated
        Assert.assertEquals(res.getMapping().getRunningVMs(n1).size(), 0);
        Assert.assertEquals(res.getMapping().getRunningVMs(n2).size(), 1);
        Assert.assertNotNull(p);
    }

    @Test
    public void testReinstantiationWithPreserve() throws SolverException {
        Mapping map = new DefaultMapping();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm5, n1);
        map.addRunningVM(vm6, n1);
        map.addRunningVM(vm7, n2);
        ShareableResource rc = new ShareableResource("cpu", 10);
        rc.set(n1, 7);
        rc.set(vm5, 3);
        rc.set(vm6, 3);
        rc.set(vm7, 5);

        Model mo = new DefaultModel(map);

        for (UUID vm : map.getAllVMs()) {
            mo.getAttributes().put(vm, "template", "small");
            mo.getAttributes().put(vm, "clone", true);
        }
        Preserve pr = new Preserve(map.getAllVMs(), "cpu", 5);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getDurationEvaluators().register(MigrateVM.class, new ConstantDuration(20));

        mo.attach(rc);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Online(map.getAllNodes()));
        cstrs.add(pr);
        cra.doOptimize(true);
        try {
            ReconfigurationPlan p = cra.solve(mo, cstrs);
            System.out.println(p);
            Assert.assertNotNull(p);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }
}
