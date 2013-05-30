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

import btrplace.model.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultReconfigurationProblemBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.kernel.solver.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link StayAwayVMModel}.
 *
 * @author Fabien Hermenier
 */
public class StayAwayVMModelTest {

    @Test
    public void testBasic() throws SolverException, ContradictionException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        final VM vm1 = mo.newVM();
        final VM vm2 = mo.newVM();
        Node n1 = mo.newNode();

        map.addOnlineNode(n1);
        map.addSleepingVM(vm1, n1);
        map.addReadyVM(vm2);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .labelVariables()
                .build();

        rp.getNodeAction(n1).getState().setVal(1);
        StayAwayVMModel ma1 = (StayAwayVMModel) rp.getVMAction(vm1);
        StayAwayVMModel ma2 = (StayAwayVMModel) rp.getVMAction(vm2);
        Assert.assertEquals(vm1, ma1.getVM());
        Assert.assertEquals(vm2, ma2.getVM());

        for (VMActionModel am : rp.getVMActions()) {
            Assert.assertTrue(am.getState().isInstantiatedTo(0));
            Assert.assertNull(am.getCSlice());
            Assert.assertNull(am.getDSlice());
            Assert.assertTrue(am.getStart().isInstantiatedTo(0));
            Assert.assertTrue(am.getEnd().isInstantiatedTo(0));
            Assert.assertTrue(am.getDuration().isInstantiatedTo(0));
        }

        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        Assert.assertEquals(0, p.getSize());
    }
}
