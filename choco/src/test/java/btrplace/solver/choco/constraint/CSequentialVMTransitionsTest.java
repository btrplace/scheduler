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
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link CSequentialVMTransitions}.
 *
 * @author Fabien Hermenier
 */
public class CSequentialVMTransitionsTest {

    @Test
    public void testWithOnlyTransitions() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).ready(vm1).run(n1, vm2, vm4).sleep(n2, vm3).get();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Running(vm1));
        cstrs.add(new Sleeping(vm2));
        cstrs.add(new Running(vm3));
        cstrs.add(new Ready(vm4));
        cstrs.addAll(Online.newOnlines(map.getAllNodes()));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        List<VM> seq = Arrays.asList(vm1, vm2, vm3, vm4);
        cstrs.add(new SequentialVMTransitions(seq));
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
    }

    @Test
    public void testWithVMsWithNoTransitions() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).ready(vm1).run(n1, vm2, vm4).run(n2, vm3).get();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Running(vm1));
        cstrs.add(new Running(vm2));
        cstrs.add(new Running(vm3));
        cstrs.add(new Ready(vm4));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        List<VM> seq = Arrays.asList(vm1, vm2, vm3, vm4);
        cstrs.add(new SequentialVMTransitions(seq));
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
        //System.out.println(plan);
    }
}
