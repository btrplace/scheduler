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
import btrplace.model.constraint.CumulatedResourceCapacity;
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.Running;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CCumulatedRunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CCumulatedResourceCapacityTest extends ConstraintTestMaterial {

    @Test
    public void testWithSatisfiedConstraint() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n3, vm3, vm4)
                .sleep(n2, vm5).get();

        btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 4);
        rc.set(vm3, 3);
        rc.set(vm4, 1);
        rc.set(vm5, 5);

        Model mo = new DefaultModel(map);
        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<SatConstraint>();
        CumulatedResourceCapacity x = new CumulatedResourceCapacity(map.getAllNodes(), "cpu", 10);
        x.setContinuous(false);
        l.add(x);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertEquals(plan.getSize(), 0);
    }

    @Test
    public void testDiscreteSatisfaction() throws SolverException {

        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4, vm5).get();

        Set<UUID> on = new HashSet<UUID>(Arrays.asList(n1, n2));

        btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 4);
        rc.set(vm3, 3);
        rc.set(vm4, 1);
        rc.set(vm5, 5);

        Model mo = new DefaultModel(map);
        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<SatConstraint>();
        CumulatedResourceCapacity x = new CumulatedResourceCapacity(on, "cpu", 10);
        x.setContinuous(false);
        l.add(x);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        //System.out.println(plan);
        Assert.assertTrue(plan.getSize() > 0);
    }

    @Test
    public void testFeasibleContinuousResolution() throws SolverException {

        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4)
                .ready(vm5).get();
        Set<UUID> on = new HashSet<UUID>(Arrays.asList(n1, n2));
        Model mo = new DefaultModel(map);

        btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 4);
        rc.set(vm3, 3);
        rc.set(vm4, 1);
        rc.set(vm5, 5);
        mo.attach(rc);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(new Running(Collections.singleton(vm5)));
        l.add(new Fence(Collections.singleton(vm5), Collections.singleton(n1)));
        CumulatedResourceCapacity x = new CumulatedResourceCapacity(on, "cpu", 10);
        x.setContinuous(true);
        l.add(x);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        //System.out.println(plan);
        Assert.assertTrue(plan.getSize() > 0);
    }

    @Test
    public void testGetMisplaced() {
        Mapping m = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm2, vm3).run(n2, vm4).ready(vm5).get();

        btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 4);
        rc.set(vm3, 3);
        rc.set(vm4, 1);
        rc.set(vm5, 5);
        Model mo = new DefaultModel(m);
        mo.attach(rc);
        CumulatedResourceCapacity c = new CumulatedResourceCapacity(m.getAllNodes(), "cpu", 10);
        CCumulatedResourceCapacity cc = new CCumulatedResourceCapacity(c);

        Assert.assertTrue(cc.getMisPlacedVMs(mo).isEmpty());
        m.addRunningVM(vm5, n3);
        Assert.assertEquals(cc.getMisPlacedVMs(mo), m.getAllVMs());
    }
}
