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
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Ready;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.MigrateVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link btrplace.model.constraint.Fence}.
 *
 * @author Fabien Hermenier
 */
public class CFenceTest {

    /**
     * Test getMisPlaced() in various situations.
     */
    @Test
    public void testGetMisPlaced() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();
        Node n5 = mo.newNode();
        Mapping m = new MappingFiller(mo.getMapping()).on(n1, n2, n3, n4)
                .off(n5)
                .run(n1, vm1, vm2)
                .run(n2, vm3)
                .run(n3, vm4)
                .sleep(n4, vm5).get();

        Set<VM> vms = new HashSet<>(Arrays.asList(vm1, vm2));
        Set<Node> ns = new HashSet<>(Arrays.asList(n1, n2));
        CFence c = new CFence(new Fence(vm1, ns));
        Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        ns.add(mo.newNode());
        Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        vms.add(vm3);
        Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        vms.add(vm4);
        Set<VM> bad = new CFence(new Fence(vm4, ns)).getMisPlacedVMs(mo);
        Assert.assertEquals(1, bad.size());
        Assert.assertTrue(bad.contains(vm4));
    }

    @Test
    public void testBasic() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm4)
                .run(n2, vm2)
                .run(n3, vm3).get();

        Set<Node> on = new HashSet<>(Arrays.asList(n1, n3));
        Fence f = new Fence(vm2, on);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(f);
        cstrs.addAll(Online.newOnline(map.getAllNodes()));
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getSize() > 0);
        Assert.assertTrue(p.iterator().next() instanceof MigrateVM);
        //Assert.assertEquals(SatConstraint.Sat.SATISFIED, f.isSatisfied(p.getResult()));
        cstrs.add(new Ready(vm2));

        p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        Assert.assertEquals(1, p.getSize()); //Just the suspend of vm2
        //Assert.assertEquals(SatConstraint.Sat.SATISFIED, f.isSatisfied(p.getResult()));


    }
}
