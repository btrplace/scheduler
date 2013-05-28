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

package btrplace.solver.choco.view;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.Preserve;
import btrplace.model.constraint.SatConstraint;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();
        ma.addOnlineNode(n1);
        ma.addOfflineNode(n2);
        ma.addRunningVM(vm1, n1);
        ma.addRunningVM(vm2, n1);
        ma.addReadyVM(vm3);
        ShareableResource rc = new ShareableResource("foo", 0, 0);
        rc.setVMConsumption(vm2, 3);
        rc.setNodeCapacity(n1, 4);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).build();
        CShareableResource rcm = new CShareableResource(rp, rc);
        Assert.assertEquals(rc.getIdentifier(), rcm.getIdentifier());
        Assert.assertEquals(-1, rcm.getVMsAllocation()[rp.getVMIdx(vm1)].getInf());
        Assert.assertEquals(-1, rcm.getVMsAllocation()[rp.getVMIdx(vm2)].getInf());
        Assert.assertEquals(0, rcm.getVMsAllocation()[rp.getVMIdx(vm3)].getSup()); //Will not be running so 0
        IntDomainVar pn1 = rcm.getPhysicalUsage()[rp.getNodeIdx(n1)];
        IntDomainVar pn2 = rcm.getPhysicalUsage()[rp.getNodeIdx(n2)];
        Assert.assertTrue(pn1.getInf() == 0 && pn1.getSup() == 4);
        Assert.assertTrue(pn2.getInf() == 0 && pn2.getSup() == 0);

        pn1 = rcm.getPhysicalUsage(rp.getNodeIdx(n1));
        Assert.assertTrue(pn1.getInf() == 0 && pn1.getSup() == 4);

        IntDomainVar vn1 = rcm.getVirtualUsage()[rp.getNodeIdx(n1)];
        IntDomainVar vn2 = rcm.getVirtualUsage()[rp.getNodeIdx(n2)];
        Assert.assertEquals(vn1.getInf(), 0);
        Assert.assertEquals(vn2.getInf(), 0);

        Assert.assertEquals(rc, rcm.getSourceResource());

    }

    /**
     * Place some VMs and check realNodeUsage is updated accordingly
     */
    @Test
    public void testRealNodeUsage() throws SolverException, ContradictionException {
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        ma.addOnlineNode(n1);
        ma.addOnlineNode(n2);
        ma.addRunningVM(vm1, n1);
        ma.addRunningVM(vm2, n1);

        ShareableResource rc = new ShareableResource("foo", 0, 0);
        rc.setVMConsumption(vm1, 2);
        rc.setVMConsumption(vm2, 3);
        rc.setNodeCapacity(n1, 5);
        rc.setNodeCapacity(n2, 3);
        mo.attach(rc);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        VMActionModel avm1 = rp.getVMActions()[rp.getVMIdx(vm1)];
        VMActionModel avm2 = rp.getVMActions()[rp.getVMIdx(vm2)];
        avm1.getDSlice().getHoster().setVal(0);
        avm2.getDSlice().getHoster().setVal(1);
        CShareableResource rcm = (CShareableResource) rp.getView(btrplace.model.view.ShareableResource.VIEW_ID_BASE + "foo");
        //Basic consumption for the VMs. If would be safe to use Preserve, but I don't want:D
        rcm.getVMsAllocation()[rp.getVMIdx(vm1)].setInf(2);
        rcm.getVMsAllocation()[rp.getVMIdx(vm2)].setInf(3);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        Assert.assertTrue(rcm.getVirtualUsage(0).isInstantiatedTo(2));
        Assert.assertTrue(rcm.getVirtualUsage(1).isInstantiatedTo(3));
    }

    @Test
    public void testMaintainResourceUsage() throws SolverException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();

        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        ShareableResource rc = new ShareableResource("foo");
        rc.setVMConsumption(vm1, 5);
        rc.setVMConsumption(vm2, 7);
        rc.setNodeCapacity(n1, 25);

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
        Assert.assertEquals(rcm.getVMsAllocation()[rp.getVMIdx(vm1)].getVal(), 5);
        Assert.assertEquals(rcm.getVMsAllocation()[rp.getVMIdx(vm2)].getVal(), 7);

        //And on the resulting plan.
        Model res = p.getResult();
        ShareableResource resRc = (ShareableResource) res.getView(rc.getIdentifier());
        Assert.assertEquals(resRc.getVMConsumption(vm1), 5);
        Assert.assertEquals(resRc.getVMConsumption(vm2), 7);
    }

    /**
     * The default overbooking ratio of 1 will make this problem having no solution.
     */
    @Test
    public void testDefaultOverbookRatio() throws ContradictionException, SolverException {
        Model mo = new DefaultModel();
        Mapping ma = new MappingFiller(mo.getMapping()).on(n1).run(n1, vm1, vm2).get();

        ShareableResource rc = new ShareableResource("foo", 0, 0);
        rc.setVMConsumption(vm1, 2);
        rc.setVMConsumption(vm2, 3);
        rc.setNodeCapacity(n1, 5);

        mo.attach(rc);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        VMActionModel avm1 = rp.getVMActions()[rp.getVMIdx(vm1)];
        avm1.getDSlice().getHoster().setVal(0);
        CShareableResource rcm = (CShareableResource) rp.getView(btrplace.model.view.ShareableResource.VIEW_ID_BASE + "foo");
        //Basic consumption for the VMs. If would be safe to use Preserve, but I don't want:D
        rcm.getVMsAllocation()[rp.getVMIdx(vm2)].setInf(4);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNull(p);
    }

    @Test
    public void testWithFloat() throws SolverException {
        Model mo = new DefaultModel();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).run(n1, vm1, vm2).get();

        btrplace.model.view.ShareableResource rc = new ShareableResource("foo");
        rc.setNodeCapacity(n1, 32);
        rc.setVMConsumption(vm1, 3);
        rc.setVMConsumption(vm2, 2);
        mo.attach(rc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Online(map.getAllNodes()));
        Overbook o = new Overbook(map.getAllNodes(), "foo", 1.5);
        o.setContinuous(false);
        cstrs.add(o);
        cstrs.add(new Preserve(Collections.singleton(vm1), "foo", 5));
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        System.out.println(p);
    }
}
