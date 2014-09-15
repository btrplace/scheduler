/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.constraint;

import btrplace.model.*;
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.Gather;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link btrplace.model.constraint.Gather}.
 *
 * @author Fabien Hermenier
 */
public class CNoDelayTest {

    @Test
    public void testDiscreteWithoutRunningVM() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).ready(vm1).on(n1, n2).run(n2, vm2).get();
        Gather g = new Gather(map.getAllVMs());
        g.setContinuous(false);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, Collections.<SatConstraint>singleton(g));
        Assert.assertNotNull(plan);
        Assert.assertEquals(plan.getSize(), 0);
        Model res = plan.getResult();
        Assert.assertTrue(res.getMapping().isReady(vm1));
        //Assert.assertEquals(g.isSatisfied(res), SatConstraint.Sat.SATISFIED);
        //Assert.assertEquals(g.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
    }

    @Test
    public void testDiscreteWithRunningVMs() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).ready(vm1).on(n1, n2).run(n2, vm2).get();
        Gather g = new Gather(map.getAllVMs());
        g.setContinuous(false);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(g);
        cstrs.addAll(Running.newRunning(map.getAllVMs()));
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
        Model res = plan.getResult();
        Assert.assertEquals(res.getMapping().getVMLocation(vm1), res.getMapping().getVMLocation(vm2));
    }

    @Test
    public void testGetMisplaced() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).ready(vm1).on(n1, n2).run(n2, vm2).get();
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
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).ready(vm1).on(n1, n2).run(n2, vm2).get();
        Gather g = new Gather(map.getAllVMs());
        g.setContinuous(true);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Running.newRunning(map.getAllVMs()));
        cstrs.add(g);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
    }

    /**
     * We try to relocate co-located VMs in continuous mode. Not allowed
     *
     * @throws btrplace.solver.SolverException
     */
    @Test
    public void testContinuousWithRelocationOfVMs() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).run(n2, vm1, vm2).get();
        Gather g = new Gather(map.getAllVMs());
        g.setContinuous(true);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Running.newRunning(map.getAllVMs()));
        cstrs.add(g);
        cstrs.add(new Fence(vm1, Collections.singleton(n1)));
        cstrs.add(new Fence(vm2, Collections.singleton(n1)));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNull(plan);
    }

    @Test
    public void testContinuousWithNoRunningVMs() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).ready(vm1, vm2).get();
        Gather g = new Gather(map.getAllVMs());
        g.setContinuous(true);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(g);
        cstrs.addAll(Running.newRunning(map.getAllVMs()));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
    }
}
