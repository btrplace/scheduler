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
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.ShutdownNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingFiller;
import btrplace.solver.choco.durationEvaluator.ConstantActionDuration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link COffline}.
 *
 * @author Fabien Hermenier
 */
public class COfflineTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        Node n1 = mo.newNode();
        Offline b = new Offline(n1);
        COffline c = new COffline(b);
        Assert.assertEquals(c.toString(), b.toString());
    }

    /**
     * Simple test, no VMs.
     */
    @Test
    public void simpleTest() throws SolverException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();

        Mapping map = new MappingFiller(model.getMapping()).on(n1, n2).get();

        DefaultChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantActionDuration(10));
        cra.setTimeLimit(-1);
        List x = Offline.newOfflines(map.getAllNodes());
        ReconfigurationPlan plan = cra.solve(model, x);
        Assert.assertNotNull(plan);
        Assert.assertEquals(plan.getSize(), 2);
        Assert.assertEquals(plan.getDuration(), 10);
        Model res = plan.getResult();
        Assert.assertEquals(res.getMapping().getOfflineNodes().size(), 2);
    }

    @Test
    public void testGetMisplacedAndSatisfied() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).get();

        Offline off = new Offline(n1);
        COffline coff = new COffline(off);

        Assert.assertTrue(coff.getMisPlacedVMs(mo).isEmpty());

        map.addRunningVM(vm1, n1);
        Assert.assertEquals(coff.getMisPlacedVMs(mo), map.getAllVMs());
    }

    @Test
    public void testSolvableProblem() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).run(n1, vm1).get();
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, Collections.<SatConstraint>singleton(new Offline(n1)));
        Assert.assertNotNull(plan);
        Model res = plan.getResult();
        Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n1));
    }

    @Test
    public void testUnsolvableProblem() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        Node n1 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1).run(n1, vm1).get();
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, Collections.<SatConstraint>singleton(new Offline(n1)));
        Assert.assertNull(plan);
    }
}
