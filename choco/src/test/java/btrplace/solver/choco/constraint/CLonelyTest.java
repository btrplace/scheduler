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

package btrplace.solver.choco.constraint;

import btrplace.model.*;
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.Lonely;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link CLonely}.
 *
 * @author Fabien Hermenier
 */
public class CLonelyTest {

    @Test
    public void testFeasibleDiscrete() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4, vm5).get();

        Set<VM> mine = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        Lonely l = new Lonely(mine);
        l.setContinuous(false);
        ReconfigurationPlan plan = cra.solve(mo, Collections.<SatConstraint>singleton(l));
        Assert.assertNotNull(plan);
        //System.out.println(plan);
        //Assert.assertEquals(l.isSatisfied(plan.getResult()), SatConstraint.Sat.SATISFIED);
    }

    /**
     * vm3 needs to go to n2 . vm1, vm2, vm3 must be lonely. n2 is occupied by "other" VMs, so
     * they have to go away before receiving vm3
     *
     * @throws SolverException
     */
    @Test
    public void testFeasibleContinuous() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2, vm3)
                .run(n2, vm4, vm5).get();
        Set<VM> mine = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        Lonely l = new Lonely(mine);
        l.setContinuous(true);
        Set<SatConstraint> cstrs = new HashSet<>();
        cstrs.add(l);
        cstrs.add(new Fence(Collections.singleton(vm3), Collections.singleton(n2)));
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
    }

    @Test
    public void testGetMisplaced() {

        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2, vm3)
                .run(n2, vm4, vm5).get();
        Set<VM> mine = new HashSet<>(Arrays.asList(vm1, vm2, vm3));


        CLonely c = new CLonely(new Lonely(mine));
        Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        map.addRunningVM(vm2, n2);
        Assert.assertEquals(c.getMisPlacedVMs(mo), map.getRunningVMs(n2));
    }
}
