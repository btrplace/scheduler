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
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SingleResourceCapacity;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CSingleResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CSingleResourceCapacityTest extends ConstraintTestMaterial {

    @Test
    public void testGetMisplaced() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n2);

        ShareableResource rc = new DefaultShareableResource("cpu", 5);
        rc.set(vm1, 3);
        rc.set(vm2, 3);
        rc.set(vm3, 1);

        Model mo = new DefaultModel(map);
        mo.attach(rc);

        Set<UUID> nodes = new HashSet<UUID>();
        nodes.add(n1);
        nodes.add(n2);

        SingleResourceCapacity s = new SingleResourceCapacity(nodes, "cpu", 4);
        CSingleResourceCapacity cs = new CSingleResourceCapacity(s);
        Assert.assertTrue(cs.getMisPlacedVMs(mo).isEmpty());
        map.addRunningVM(vm2, n1);
        Assert.assertEquals(cs.getMisPlacedVMs(mo), map.getRunningVMs(n1));
    }

    @Test
    public void testDiscreteSolvable() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);

        ShareableResource rc = new DefaultShareableResource("cpu", 5);
        rc.set(vm1, 3);
        rc.set(vm2, 3);
        rc.set(vm3, 1);

        Model mo = new DefaultModel(map);
        mo.attach(rc);

        Set<UUID> nodes = new HashSet<UUID>();
        nodes.add(n1);
        nodes.add(n2);

        SingleResourceCapacity s = new SingleResourceCapacity(nodes, "cpu", 4);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>singleton(s));
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 1);
        System.out.println(p);
    }

    @Test
    public void testDiscreteUnsolvable() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);

        ShareableResource rc = new DefaultShareableResource("cpu", 5);
        rc.set(vm1, 3);
        rc.set(vm2, 3);
        rc.set(vm3, 1);

        Model mo = new DefaultModel(map);
        mo.attach(rc);

        Set<UUID> nodes = new HashSet<UUID>();
        nodes.add(n1);
        nodes.add(n2);

        SingleResourceCapacity s = new SingleResourceCapacity(nodes, "cpu", 3);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>singleton(s));
        Assert.assertNull(p);
    }

    @Test
    public void testContinuousSolvable() throws SolverException {
        ChocoLogging.setVerbosity(Verbosity.FINEST);
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n2);
        map.addReadyVM(vm4);
        ShareableResource rc = new DefaultShareableResource("cpu", 5);
        rc.set(vm1, 3);
        rc.set(vm2, 1);
        rc.set(vm3, 1);
        rc.set(vm4, 3);

        Model mo = new DefaultModel(map);
        mo.attach(rc);

        Set<UUID> nodes = new HashSet<UUID>();
        nodes.add(n1);
        nodes.add(n2);

        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();

        SingleResourceCapacity s = new SingleResourceCapacity(nodes, "cpu", 4);
        s.setContinuous(true);

        cstrs.add(s);
        cstrs.add(new Fence(Collections.singleton(vm4), Collections.singleton(n2)));
        cstrs.add(new Running(Collections.singleton(vm4)));

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(p.getSize(), 2);

    }
}
