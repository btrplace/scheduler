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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;


/**
 * Unit tests for {@link StayRunningVMModel}.
 *
 * @author Fabien Hermenier
 */
public class StayRunningVMModelTest {

    @Test
    public void testBasic() throws SolverException {

        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        final VM vm1 = mo.newVM();
        Node n1 = mo.newNode();

        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setManageableVMs(Collections.<VM>emptySet())
                .labelVariables()
                .build();
        Assert.assertEquals(rp.getVMAction(vm1).getClass(), StayRunningVMModel.class);
        StayRunningVMModel m1 = (StayRunningVMModel) rp.getVMAction(vm1);
        Assert.assertNotNull(m1.getCSlice());
        Assert.assertNotNull(m1.getDSlice());
        Assert.assertTrue(m1.getCSlice().getHoster().instantiatedTo(rp.getNode(n1)));
        Assert.assertTrue(m1.getDSlice().getHoster().instantiatedTo(rp.getNode(n1)));
        Assert.assertTrue(m1.getDuration().instantiatedTo(0));
        Assert.assertTrue(m1.getStart().instantiatedTo(0));
        Assert.assertTrue(m1.getEnd().instantiatedTo(0));
        System.out.println(rp.getSolver().toString());
        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 0);
    }
}
