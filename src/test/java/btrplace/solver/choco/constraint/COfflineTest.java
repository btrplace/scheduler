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
import btrplace.model.constraint.Offline;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.ShutdownNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link COffline}.
 *
 * @author Fabien Hermenier
 */
public class COfflineTest extends ConstraintTestMaterial {

    @Test
    public void testInstantiation() {
        Offline b = new Offline(Collections.singleton(UUID.randomUUID()));
        COffline c = new COffline(b);
        //Assert.assertEquals(b, c.getAssociatedConstraint());
        Assert.assertEquals(c.toString(), b.toString());
    }

    /**
     * Simple test, no VMs.
     */
    @Test
    public void simpleTest() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);

        Model model = new DefaultModel(map);
        DefaultChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(10));
        cra.setTimeLimit(-1);
        Collection<SatConstraint> x = Collections.singleton((SatConstraint) new Offline(map.getAllNodes()));
        ReconfigurationPlan plan = cra.solve(model, x);
        Assert.assertNotNull(plan);
        Assert.assertEquals(plan.getSize(), 2);
        Assert.assertEquals(plan.getDuration(), 10);
        Model res = plan.getResult();
        Assert.assertEquals(res.getMapping().getOfflineNodes().size(), 2);
    }

    @Test
    public void testGetMisplacedAndSatisfied() {
        Mapping map = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);

        Set<UUID> s = new HashSet<UUID>();
        s.add(n1);
        s.add(n2);
        Offline off = new Offline(s);
        COffline coff = new COffline(off);
        Model mo = new DefaultModel(map);

        Assert.assertTrue(coff.getMisPlacedVMs(mo).isEmpty());

        UUID vm = UUID.randomUUID();
        map.addRunningVM(vm, n1);
        Assert.assertEquals(coff.getMisPlacedVMs(mo), map.getAllVMs());
               /*
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertFalse(coff.isSatisfied(plan));
        plan.add(new ShutdownNode(n2, 0, 1));
        plan.add(new ShutdownVM(vm, n1, 0, 1));
        Assert.assertFalse(coff.isSatisfied(plan));
        plan.add(new ShutdownNode(n1, 1, 2));
        Assert.assertTrue(coff.isSatisfied(plan));
        */
    }

    @Test
    public void testSolvableProblem() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        Model mo = new DefaultModel(map);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, Collections.<SatConstraint>singleton(new Offline(Collections.singleton(n1))));
        Assert.assertNotNull(plan);
        Model res = plan.getResult();
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n1));
    }

    @Test
    public void testUnsolvableProblem() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        Model mo = new DefaultModel(map);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, Collections.<SatConstraint>singleton(new Offline(Collections.singleton(n1))));
        Assert.assertNull(plan);
    }
}
