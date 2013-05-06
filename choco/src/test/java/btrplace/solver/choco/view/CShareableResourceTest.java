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

package btrplace.solver.choco.view;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.*;
import btrplace.solver.choco.actionModel.VMActionModel;
import btrplace.test.PremadeElements;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * Unit tests for {@link CShareableResource}.
 *
 * @author Fabien Hermenier
 */
public class CShareableResourceTest implements PremadeElements {

    /**
     * Test the instantiation and the creation of the variables.
     *
     * @throws SolverException should not occur
     */
    @Test
    public void testSimple() throws SolverException {
        Mapping ma = new DefaultMapping();
        ma.addOnlineNode(n1);
        ma.addOfflineNode(n2);
        ma.addRunningVM(vm1, n1);
        ma.addRunningVM(vm2, n1);
        ma.addReadyVM(vm3);
        ShareableResource rc = new ShareableResource("foo", 0);
        rc.set(vm2, 3);
        rc.set(n1, 4);
        Model mo = new DefaultModel(ma);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).build();
        CShareableResource rcm = new CShareableResource(rp, rc);
        Assert.assertEquals(rc.getIdentifier(), rcm.getIdentifier());
        Assert.assertEquals(-1, rcm.getVMsAllocation()[rp.getVM(vm1)].getInf());
        Assert.assertEquals(-1, rcm.getVMsAllocation()[rp.getVM(vm2)].getInf());
        Assert.assertEquals(0, rcm.getVMsAllocation()[rp.getVM(vm3)].getSup()); //Will not be running so 0
        IntDomainVar pn1 = rcm.getPhysicalUsage()[rp.getNode(n1)];
        IntDomainVar pn2 = rcm.getPhysicalUsage()[rp.getNode(n2)];
        Assert.assertTrue(pn1.getInf() == 0 && pn1.getSup() == 4);
        Assert.assertTrue(pn2.getInf() == 0 && pn2.getSup() == 0);

        pn1 = rcm.getPhysicalUsage(rp.getNode(n1));
        Assert.assertTrue(pn1.getInf() == 0 && pn1.getSup() == 4);

        IntDomainVar vn1 = rcm.getVirtualUsage()[rp.getNode(n1)];
        IntDomainVar vn2 = rcm.getVirtualUsage()[rp.getNode(n2)];
        Assert.assertEquals(vn1.getInf(), 0);
        Assert.assertEquals(vn2.getInf(), 0);

        Assert.assertEquals(rc, rcm.getSourceResource());

    }

    /**
     * Place some VMs and check realNodeUsage is updated accordingly
     */
    @Test
    public void testRealNodeUsage() throws SolverException, ContradictionException {
        Mapping ma = new DefaultMapping();

        ma.addOnlineNode(n1);
        ma.addOnlineNode(n2);
        ma.addRunningVM(vm1, n1);
        ma.addRunningVM(vm2, n1);

        ShareableResource rc = new ShareableResource("foo", 0);
        rc.set(vm1, 2);
        rc.set(vm2, 3);
        rc.set(n1, 5);
        rc.set(n2, 3);
        Model mo = new DefaultModel(ma);
        mo.attach(rc);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        VMActionModel avm1 = rp.getVMActions()[rp.getVM(vm1)];
        VMActionModel avm2 = rp.getVMActions()[rp.getVM(vm2)];
        avm1.getDSlice().getHoster().setVal(0);
        avm2.getDSlice().getHoster().setVal(1);
        CShareableResource rcm = (CShareableResource) rp.getView(btrplace.model.view.ShareableResource.VIEW_ID_BASE + "foo");
        //Basic consumption for the VMs. If would be safe to use Preserve, but I don't want:D
        rcm.getVMsAllocation()[rp.getVM(vm1)].setInf(2);
        rcm.getVMsAllocation()[rp.getVM(vm2)].setInf(3);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        Assert.assertTrue(rcm.getVirtualUsage(0).isInstantiatedTo(2));
        Assert.assertTrue(rcm.getVirtualUsage(1).isInstantiatedTo(3));
    }

    @Test
    public void testMaintainResourceUsage() throws SolverException {
        Mapping map = new DefaultMapping();

        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        ShareableResource rc = new ShareableResource("foo");
        rc.set(vm1, 5);
        rc.set(vm2, 7);
        rc.set(n1, 25);

        Model mo = new DefaultModel(map);
        mo.attach(rc);

        ModelViewMapper vMapper = new ModelViewMapper();
        vMapper.register(new CShareableResource.Builder());
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setViewMapper(vMapper)
                .labelVariables()
                .build();
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        //Check the amount of allocated resources on the RP
        CShareableResource rcm = (CShareableResource) rp.getView(rc.getIdentifier());
        Assert.assertEquals(rcm.getVMsAllocation()[rp.getVM(vm1)].getVal(), 5);
        Assert.assertEquals(rcm.getVMsAllocation()[rp.getVM(vm2)].getVal(), 7);

        //And on the resulting plan.
        Model res = p.getResult();
        ShareableResource resRc = (ShareableResource) res.getView(rc.getIdentifier());
        Assert.assertEquals(resRc.get(vm1), 5);
        Assert.assertEquals(resRc.get(vm2), 7);
    }

    /**
     * The default overbooking ratio of 1 will make this problem having no solution.
     */
    @Test
    public void testDefaultOverbookRatio() throws ContradictionException, SolverException {
        Mapping ma = new MappingBuilder().on(n1).run(n1, vm1, vm2).build();

        ShareableResource rc = new ShareableResource("foo", 0);
        rc.set(vm1, 2);
        rc.set(vm2, 3);
        rc.set(n1, 5);
        Model mo = new DefaultModel(ma);
        mo.attach(rc);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        VMActionModel avm1 = rp.getVMActions()[rp.getVM(vm1)];
        avm1.getDSlice().getHoster().setVal(0);
        CShareableResource rcm = (CShareableResource) rp.getView(btrplace.model.view.ShareableResource.VIEW_ID_BASE + "foo");
        //Basic consumption for the VMs. If would be safe to use Preserve, but I don't want:D
        rcm.getVMsAllocation()[rp.getVM(vm2)].setInf(4);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNull(p);
    }

    @Test
    public void testSymmetryBreaking() throws SolverException {
        ShareableResource cpu = new ShareableResource("cpu");
        ShareableResource mem = new ShareableResource("mem");
        cpu.set(n1, 10);
        mem.set(n1, 10);
        cpu.set(n2, 10);
        mem.set(n2, 10);

        cpu.set(vm1, 5);
        mem.set(vm1, 4);

        cpu.set(vm2, 3);
        mem.set(vm2, 8);

        cpu.set(vm3, 5);
        cpu.set(vm3, 4);

        cpu.set(vm4, 4);
        cpu.set(vm4, 5);

        //vm1 requires more cpu resources, but fewer mem resources
        Preserve pCPU = new Preserve(new HashSet<>(Arrays.asList(vm1, vm3)), "cpu", 7);
        Preserve pMem = new Preserve(new HashSet<>(Arrays.asList(vm1, vm3)), "mem", 2);


        Mapping map = new MappingBuilder().on(n1, n2)
                .run(n1, vm1)
                .run(n2, vm3, vm4)
                .ready(vm2).build();
        Model mo = new DefaultModel(map);
        mo.attach(cpu);
        mo.attach(mem);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setMaxEnd(5);
        cra.setVerbosity(2);
        ReconfigurationPlan p = cra.solve(mo, Arrays.<SatConstraint>asList(pCPU, pMem,
                new Online(Collections.singleton(n1)),
                new Running(Collections.singleton(vm2)),
                new Ready(Collections.singleton(vm3))));
        Assert.assertNotNull(p);
        System.out.println(p);
    }
}
