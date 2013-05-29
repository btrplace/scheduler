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

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.CumulatedResourceCapacity;
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingFiller;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CCumulatedRunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CCumulatedResourceCapacityTest implements PremadeElements {

    @Test
    public void testWithSatisfiedConstraint() throws SolverException {
        Model mo = new DefaultModel();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n3, vm3, vm4)
                .sleep(n2, vm5).get();

        ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 3);
        rc.setConsumption(vm4, 1);
        rc.setConsumption(vm5, 5);

        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<>();
        CumulatedResourceCapacity x = new CumulatedResourceCapacity(map.getAllNodes(), "cpu", 10);
        x.setContinuous(false);
        l.add(x);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        Assert.assertEquals(plan.getSize(), 0);
    }

    @Test
    public void testDiscreteSatisfaction() throws SolverException {
        Model mo = new DefaultModel();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4, vm5).get();

        Set<Integer> on = new HashSet<>(Arrays.asList(n1, n2));

        btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 3);
        rc.setConsumption(vm4, 1);
        rc.setConsumption(vm5, 1);

        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<>();
        CumulatedResourceCapacity x = new CumulatedResourceCapacity(on, "cpu", 9);
        x.setContinuous(false);
        l.add(x);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        Assert.assertTrue(plan.getSize() > 0);
    }

    @Test
    public void testFeasibleContinuousResolution() throws SolverException {

        Model mo = new DefaultModel();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4)
                .ready(vm5).get();
        Set<Integer> on = new HashSet<>(Arrays.asList(n1, n2));

        btrplace.model.view.ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 3);
        rc.setConsumption(vm4, 1);
        rc.setConsumption(vm5, 5);
        mo.attach(rc);

        List<SatConstraint> l = new ArrayList<>();
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
        Model mo = new DefaultModel();
        Mapping m = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1, vm2, vm3).run(n2, vm4).ready(vm5).get();

        ShareableResource rc = new ShareableResource("cpu", 5, 5);
        rc.setConsumption(vm1, 2);
        rc.setConsumption(vm2, 3);
        rc.setConsumption(vm3, 3);
        rc.setConsumption(vm4, 1);
        rc.setConsumption(vm5, 5);
        mo.attach(rc);
        CumulatedResourceCapacity c = new CumulatedResourceCapacity(m.getAllNodes(), "cpu", 10);
        CCumulatedResourceCapacity cc = new CCumulatedResourceCapacity(c);

        Assert.assertTrue(cc.getMisPlacedVMs(mo).isEmpty());
        m.addRunningVM(vm5, n3);
        Assert.assertEquals(cc.getMisPlacedVMs(mo), m.getAllVMs());
    }
}
