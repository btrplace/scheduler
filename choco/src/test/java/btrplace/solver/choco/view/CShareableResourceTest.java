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

import btrplace.model.*;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.Preserve;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.*;
import btrplace.solver.choco.actionModel.VMActionModel;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link CShareableResource}.
 *
 * @author Fabien Hermenier
 */
public class CShareableResourceTest {

    /**
     * Test the instantiation and the creation of the variables.
     *
     * @throws SolverException should not occur
     */
    @Test
    public void testSimple() throws SolverException {
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();

        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        ma.addOnlineNode(n1);
        ma.addOfflineNode(n2);
        ma.addRunningVM(vm1, n1);
        ma.addRunningVM(vm2, n1);
        ma.addReadyVM(vm3);
        ShareableResource rc = new ShareableResource("foo", 0, 0);
        rc.setConsumption(vm2, 3);
        rc.setCapacity(n1, 4);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).build();
        CShareableResource rcm = new CShareableResource(rp, rc);
        Assert.assertEquals(rc.getIdentifier(), rcm.getIdentifier());
        Assert.assertEquals(-1, rcm.getVMsAllocation()[rp.getVM(vm1)].getLB());
        Assert.assertEquals(-1, rcm.getVMsAllocation()[rp.getVM(vm2)].getLB());
        Assert.assertEquals(0, rcm.getVMsAllocation()[rp.getVM(vm3)].getUB()); //Will not be running so 0
        IntVar pn1 = rcm.getPhysicalUsage()[rp.getNode(n1)];
        IntVar pn2 = rcm.getPhysicalUsage()[rp.getNode(n2)];
        Assert.assertTrue(pn1.getLB() == 0 && pn1.getUB() == 4);
        Assert.assertTrue(pn2.getLB() == 0 && pn2.getUB() == 0);

        pn1 = rcm.getPhysicalUsage(rp.getNode(n1));
        Assert.assertTrue(pn1.getLB() == 0 && pn1.getUB() == 4);

        IntVar vn1 = rcm.getVirtualUsage()[rp.getNode(n1)];
        IntVar vn2 = rcm.getVirtualUsage()[rp.getNode(n2)];
        Assert.assertEquals(vn1.getLB(), 0);
        Assert.assertEquals(vn2.getLB(), 0);

        Assert.assertEquals(rc, rcm.getSourceResource());

    }

    /**
     * Place some VMs and check realNodeUsage is updated accordingly
     */
    @Test
    public void testRealNodeUsage() throws SolverException, ContradictionException {
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();

        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        ma.addOnlineNode(n1);
        ma.addOnlineNode(n2);
        ma.addRunningVM(vm1, n1);
        ma.addRunningVM(vm2, n1);

        ShareableResource rc = new ShareableResource("foo", 0, 0);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setCapacity(n1, 5);
        rc.setCapacity(n2, 3);
        mo.attach(rc);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        VMActionModel avm1 = rp.getVMActions()[rp.getVM(vm1)];
        VMActionModel avm2 = rp.getVMActions()[rp.getVM(vm2)];
        avm1.getDSlice().getHoster().instantiateTo(0, null);
        avm2.getDSlice().getHoster().instantiateTo(1, null);
        CShareableResource rcm = (CShareableResource) rp.getView(btrplace.model.view.ShareableResource.VIEW_ID_BASE + "foo");
        //Basic consumption for the VMs. If would be safe to use Preserve, but I don't want:D
        rcm.getVMsAllocation()[rp.getVM(vm1)].updateLowerBound(2, null);
        rcm.getVMsAllocation()[rp.getVM(vm2)].updateLowerBound(3, null);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        Assert.assertTrue(rcm.getVirtualUsage(0).instantiatedTo(2));
        Assert.assertTrue(rcm.getVirtualUsage(1).instantiatedTo(3));
    }

    @Test
    public void testMaintainResourceUsage() throws SolverException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();

        Node n1 = mo.newNode();

        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        ShareableResource rc = new ShareableResource("foo");
        rc.setConsumption(vm1, 5);
        rc.setConsumption(vm2, 7);
        rc.setCapacity(n1, 25);

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
        Assert.assertEquals(rcm.getVMsAllocation()[rp.getVM(vm1)].getValue(), 5);
        Assert.assertEquals(rcm.getVMsAllocation()[rp.getVM(vm2)].getValue(), 7);

        //And on the resulting plan.
        Model res = p.getResult();
        ShareableResource resRc = (ShareableResource) res.getView(rc.getIdentifier());
        Assert.assertEquals(resRc.getConsumption(vm1), 5);
        Assert.assertEquals(resRc.getConsumption(vm2), 7);
    }

    /**
     * The default overbooking ratio of 1 will make this problem having no solution.
     */
    @Test
    public void testDefaultOverbookRatio() throws ContradictionException, SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();

        Mapping ma = new MappingFiller(mo.getMapping()).on(n1).run(n1, vm1, vm2).get();

        ShareableResource rc = new ShareableResource("foo", 0, 0);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setCapacity(n1, 5);

        mo.attach(rc);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        VMActionModel avm1 = rp.getVMActions()[rp.getVM(vm1)];
        avm1.getDSlice().getHoster().instantiateTo(0, null);
        CShareableResource rcm = (CShareableResource) rp.getView(btrplace.model.view.ShareableResource.VIEW_ID_BASE + "foo");
        //Basic consumption for the VMs. If would be safe to use Preserve, but I don't want:D
        rcm.getVMsAllocation()[rp.getVM(vm2)].updateLowerBound(4, null);
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNull(p);
    }

    @Test
    public void testWithFloat() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).run(n1, vm1, vm2).get();

        btrplace.model.view.ShareableResource rc = new ShareableResource("foo");
        rc.setCapacity(n1, 32);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 2);
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
