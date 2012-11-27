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
        ma.addOnlineNode(n1);
        ma.addOfflineNode(n2);
        ma.setVMRunOn(vm, n1);
        ma.setVMRunOn(vm2, n1);

        StackableResource rc = new DefaultStackableResource("foo", 0);
        rc.set(vm2, 3);
        rc.set(n1, 4);
        Model mo = new DefaultModel(ma);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).build();
        ResourceMapping rcm = new ResourceMapping(rp, rc);
        Assert.assertEquals(rc.getIdentifier(), rcm.getIdentifier());
        Assert.assertEquals(0, rcm.getVMConsumption()[rp.getVM(vm)].getInf());
        Assert.assertEquals(3, rcm.getVMConsumption()[rp.getVM(vm2)].getInf());
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
}
