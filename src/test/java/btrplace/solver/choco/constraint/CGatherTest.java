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
import btrplace.model.constraint.Gather;
import btrplace.model.constraint.Running;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link Gather}.
 *
 * @author Fabien Hermenier
 */
public class CGatherTest extends ConstraintTestMaterial {

    @Test
    public void testDiscreteWithoutRunningVM() throws SolverException {
        Mapping map = new MappingBuilder().ready(vm1).on(n1, n2).run(n2, vm2).get();
        Model mo = new DefaultModel(map);
        Gather g = new Gather(map.getAllVMs());
        g.setContinuous(false);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, Collections.<SatConstraint>singleton(g));
        Assert.assertNotNull(plan);
        Assert.assertEquals(plan.getSize(), 0);
        Model res = plan.getResult();
        Assert.assertTrue(res.getMapping().getReadyVMs().contains(vm1));
        //Assert.assertEquals(g.isSatisfied(res), SatConstraint.Sat.SATISFIED);
        //Assert.assertEquals(g.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
    }

    @Test
    public void testDiscreteWithRunningVMs() throws SolverException {
        Mapping map = new MappingBuilder().ready(vm1).on(n1, n2).run(n2, vm2).get();
        Model mo = new DefaultModel(map);
        Gather g = new Gather(map.getAllVMs());
        g.setContinuous(false);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(g);
        cstrs.add(new Running(map.getAllVMs()));
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
        Model res = plan.getResult();
//        Assert.assertEquals(g.isSatisfied(res), SatConstraint.Sat.SATISFIED);
//        Assert.assertEquals(g.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
        Assert.assertEquals(res.getMapping().getVMLocation(vm1), res.getMapping().getVMLocation(vm2));
    }

    @Test
    public void testGetMisplaced() {
        Mapping map = new MappingBuilder().ready(vm1).on(n1, n2).run(n2, vm2).get();
        Model mo = new DefaultModel(map);
        Gather g = new Gather(map.getAllVMs());
        CGather c = new CGather(g);
        Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        map.addRunningVM(vm1, n2);
        Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());

        map.addRunningVM(vm1, n1);
        Assert.assertEquals(c.getMisPlacedVMs(mo), map.getAllVMs());
    }

    @Test
    public void testContinuousWithPartialRunning() throws SolverException {
        Mapping map = new MappingBuilder().ready(vm1).on(n1, n2).run(n2, vm2).get();
        Model mo = new DefaultModel(map);
        Gather g = new Gather(map.getAllVMs());
        g.setContinuous(true);
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(new Running(map.getAllVMs()));
        cstrs.add(g);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
    }

    /**
     * We try to relocate co-located VMs in continuous mode. Not allowed
     *
     * @throws SolverException
     */
    @Test
    public void testContinuousWithRelocationOfVMs() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2).run(n2, vm1, vm2).get();
        Model mo = new DefaultModel(map);
        Gather g = new Gather(map.getAllVMs());
        g.setContinuous(true);
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(new Running(map.getAllVMs()));
        cstrs.add(g);
        cstrs.add(new Fence(map.getAllVMs(), Collections.singleton(n1)));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNull(plan);
    }
}
