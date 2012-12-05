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

package btrplace.solver.choco;

import btrplace.model.*;
import btrplace.solver.SolverException;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link ResourceMapping}.
 *
 * @author Fabien Hermenier
 */
public class ResourceMappingTest {

    /**
     * Test the instantiation and the creation of the variables.
     *
     * @throws SolverException should not occur
     */
    @Test
    public void testSimple() throws SolverException {
        Mapping ma = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID vm = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        ma.addOnlineNode(n1);
        ma.addOfflineNode(n2);
        ma.addRunningVM(vm, n1);
        ma.addRunningVM(vm2, n1);
        ma.addReadyVM(vm3);
        ShareableResource rc = new DefaultShareableResource("foo", 0);
        rc.set(vm2, 3);
        rc.set(n1, 4);
        Model mo = new DefaultModel(ma);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).build();
        ResourceMapping rcm = new ResourceMapping(rp, rc);
        Assert.assertEquals(rc.getIdentifier(), rcm.getIdentifier());
        Assert.assertEquals(0, rcm.getVMConsumption()[rp.getVM(vm)].getInf());
        Assert.assertEquals(0, rcm.getVMConsumption()[rp.getVM(vm2)].getInf());
        Assert.assertEquals(0, rcm.getVMConsumption()[rp.getVM(vm3)].getSup()); //Will not be running so 0
        IntDomainVar pn1 = rcm.getRawNodeUsage()[rp.getNode(n1)];
        IntDomainVar pn2 = rcm.getRawNodeUsage()[rp.getNode(n2)];
        Assert.assertTrue(pn1.getInf() == 0 && pn1.getSup() == 4);
        Assert.assertTrue(pn2.getInf() == 0 && pn2.getSup() == 0);

        IntDomainVar vn1 = rcm.getRealNodeUsage()[rp.getNode(n1)];
        IntDomainVar vn2 = rcm.getRealNodeUsage()[rp.getNode(n2)];
        Assert.assertTrue(vn1.getInf() == 0);
        Assert.assertTrue(vn2.getInf() == 0);

        Assert.assertEquals(rc, rcm.getSourceResource());
    }

    /**
     * Place some VMs and check realNodeUsage is updated accordingly
     */
    @Test
    public void testRealNodeUsage() throws SolverException, ContradictionException {
        Mapping ma = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID vm = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        ma.addOnlineNode(n1);
        ma.addOnlineNode(n2);
        ma.addRunningVM(vm, n1);
        ma.addRunningVM(vm2, n1);

        ShareableResource rc = new DefaultShareableResource("foo", 0);
        rc.set(vm, 2);
        rc.set(vm2, 3);
        rc.set(n1, 4);
        rc.set(n2, 2);
        Model mo = new DefaultModel(ma);
        mo.attach(rc);
        //new Running(ma.getAllVMs())
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).labelVariables().build();
        ActionModel avm1 = rp.getVMActions()[rp.getVM(vm)];
        ActionModel avm2 = rp.getVMActions()[rp.getVM(vm2)];
        avm1.getDSlice().getHoster().setVal(0);
        avm2.getDSlice().getHoster().setVal(1);
        ResourceMapping rcm = rp.getResourceMapping("foo");
        //Basic consumption for the VMs. If would be safe to use Preserve, but I don't want:D
        rcm.getVMConsumption()[0].setInf(2);
        rcm.getVMConsumption()[1].setInf(3);
        rp.getSolver().solve();
        Assert.assertEquals(2, rcm.getRealNodeUsage()[0].getInf());
        Assert.assertEquals(2, rcm.getRealNodeUsage()[0].getSup());
        Assert.assertEquals(3, rcm.getRealNodeUsage()[1].getInf());
        Assert.assertEquals(3, rcm.getRealNodeUsage()[1].getSup());
    }

}
