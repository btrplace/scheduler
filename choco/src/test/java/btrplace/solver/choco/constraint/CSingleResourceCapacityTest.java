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

package btrplace.solver.choco.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SingleResourceCapacity;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CSingleResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CSingleResourceCapacityTest implements PremadeElements {

    @Test
    public void testGetMisplaced() {
        Mapping map = new MappingBuilder().on(n1, n2, n3).run(n1, vm1).run(n2, vm2, vm3).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5);
        rc.set(vm1, 3);
        rc.set(vm2, 3);
        rc.set(vm3, 1);

        Model mo = new DefaultModel(map);
        mo.attach(rc);

        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2));

        SingleResourceCapacity s = new SingleResourceCapacity(nodes, "cpu", 4);
        CSingleResourceCapacity cs = new CSingleResourceCapacity(s);
        Assert.assertTrue(cs.getMisPlacedVMs(mo).isEmpty());
        map.addRunningVM(vm2, n1);
        Assert.assertEquals(cs.getMisPlacedVMs(mo), map.getRunningVMs(n1));
    }

    @Test
    public void testDiscreteSolvable() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2).run(n1, vm1, vm2).run(n2, vm3).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5);
        rc.set(vm1, 3);
        rc.set(vm2, 3);
        rc.set(vm3, 1);

        Model mo = new DefaultModel(map);
        mo.attach(rc);

        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2));

        SingleResourceCapacity s = new SingleResourceCapacity(nodes, "cpu", 4);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>singleton(s));
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 1);
        //System.out.println(p);
    }

    @Test
    public void testDiscreteUnsolvable() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2).run(n1, vm1, vm2).run(n2, vm3).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5);
        rc.set(vm1, 3);
        rc.set(vm2, 3);
        rc.set(vm3, 1);

        Model mo = new DefaultModel(map);
        mo.attach(rc);

        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2));

        SingleResourceCapacity s = new SingleResourceCapacity(nodes, "cpu", 3);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>singleton(s));
        Assert.assertNull(p);
    }

    @Test
    public void testContinuousSolvable() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2).run(n1, vm1).run(n2, vm2, vm3).ready(vm4).build();
        btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5);
        rc.set(vm1, 3);
        rc.set(vm2, 1);
        rc.set(vm3, 1);
        rc.set(vm4, 3);

        Model mo = new DefaultModel(map);
        mo.attach(rc);

        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2));

        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();

        SingleResourceCapacity s = new SingleResourceCapacity(nodes, "cpu", 4);
        s.setContinuous(true);

        cstrs.add(s);
        cstrs.add(new Fence(Collections.singleton(vm4), Collections.singleton(n2)));
        cstrs.add(new Running(Collections.singleton(vm4)));
        cstrs.add(new Overbook(map.getAllNodes(), "cpu", 1));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        //System.out.println(p);
        Assert.assertEquals(p.getSize(), 2);

    }
}
