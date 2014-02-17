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
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CSingleResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CSingleResourceCapacityTest {

    @Test
    public void testGetMisplaced() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3).run(n1, vm1).run(n2, vm2, vm3).get();

        btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 1);

        mo.attach(rc);

        Set<Node> nodes = new HashSet<>(Arrays.asList(n1, n2));

        SingleResourceCapacity s = new SingleResourceCapacity(nodes, "cpu", 4);
        CSingleResourceCapacity cs = new CSingleResourceCapacity(s);
        Assert.assertTrue(cs.getMisPlacedVMs(mo).isEmpty());
        map.addRunningVM(vm2, n1);
        Assert.assertEquals(cs.getMisPlacedVMs(mo), map.getRunningVMs(n1));
    }

    @Test
    public void testDiscreteSolvable() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).run(n1, vm1, vm2).run(n2, vm3).get();

        ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 1);
        rc.setConsumption(vm3, 1);

        mo.attach(rc);

        Set<Node> nodes = new HashSet<>(Arrays.asList(n1, n2));

        SingleResourceCapacity s = new SingleResourceCapacity(nodes, "cpu", 4);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, Arrays.asList(s, new Preserve(Collections.singleton(vm2), "cpu", 3)));
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 1);
        //System.out.println(p);
    }

    @Test
    public void testDiscreteUnsolvable() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).run(n1, vm1, vm2).run(n2, vm3).get();

        btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 1);

        mo.attach(rc);

        Set<Node> nodes = new HashSet<>(Arrays.asList(n1, n2));

        SingleResourceCapacity s = new SingleResourceCapacity(nodes, "cpu", 3);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>singleton(s));
        Assert.assertNull(p);
    }

    @Test
    public void testContinuousSolvable() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).run(n1, vm1).run(n2, vm2, vm3).ready(vm4).get();
        ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 1);
        rc.setConsumption(vm3, 1);
        rc.setConsumption(vm4, 3);

        mo.attach(rc);

        Set<Node> nodes = new HashSet<>(Arrays.asList(n1, n2));

        List<SatConstraint> cstrs = new ArrayList<>();

        SingleResourceCapacity s = new SingleResourceCapacity(nodes, "cpu", 4);
        s.setContinuous(true);

        cstrs.add(s);
        cstrs.add(new Fence(vm4, Collections.singleton(n2)));
        cstrs.add(new Running(vm4));
        cstrs.add(new Overbook(map.getAllNodes(), "cpu", 1));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        //System.out.println(p);
        Assert.assertEquals(p.getSize(), 2);

    }
}
