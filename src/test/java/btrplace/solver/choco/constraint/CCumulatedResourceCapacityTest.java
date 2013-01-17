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

import btrplace.model.*;
import btrplace.model.constraint.CumulatedResourceCapacity;
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.Running;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
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
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n3);
        map.addRunningVM(vm4, n3);
        map.addSleepingVM(vm5, n2);

        ShareableResource rc = new DefaultShareableResource("cpu", 5);
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
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n2);
        map.addRunningVM(vm5, n2);
        Set<UUID> on = new HashSet<UUID>();
        on.add(n1);
        on.add(n2);

        ShareableResource rc = new DefaultShareableResource("cpu", 5);
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
        System.out.println(plan);
        Assert.assertTrue(plan.getSize() > 0);
    }

    @Test
    public void testFeasibleContinuousResolution() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n2);
        map.addReadyVM(vm5);
        Set<UUID> on = new HashSet<UUID>();
        on.add(n1);
        on.add(n2);
        Model mo = new DefaultModel(map);

        ShareableResource rc = new DefaultShareableResource("cpu", 5);
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
        System.out.println(plan);
        Assert.assertTrue(plan.getSize() > 0);
    }

    @Test
    public void testGetMisplaced() {
        Mapping m = new DefaultMapping();
        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);
        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm2, n1);
        m.addRunningVM(vm3, n1);
        m.addRunningVM(vm4, n2);
        m.addReadyVM(vm5);

        ShareableResource rc = new DefaultShareableResource("cpu", 5);
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
