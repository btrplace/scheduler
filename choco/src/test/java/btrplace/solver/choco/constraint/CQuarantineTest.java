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
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.Quarantine;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CQuarantine}.
 *
 * @author Fabien Hermenier
 */
public class CQuarantineTest {

    @Test
    public void testWithSatisfiedModel() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3).run(n1, vm1).run(n2, vm2, vm3).run(n3, vm4).get();
        Set<Node> ns = new HashSet<>(Arrays.asList(n1, n2));
        Quarantine q = new Quarantine(ns);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>singleton(q));
        Assert.assertNotNull(p);
    }

    /**
     * A VM try to come into the quarantine zone.
     *
     * @throws SolverException
     */
    @Test
    public void testWithNoSolution1() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3).run(n1, vm1).run(n2, vm2, vm3).run(n3, vm4).get();
        Set<Node> ns = new HashSet<>(Arrays.asList(n1, n2));
        Quarantine q = new Quarantine(ns);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(q);
        cstrs.add(new Fence(Collections.singleton(vm4), Collections.singleton(n1)));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNull(p);
    }

    /**
     * A VM try to leave the quarantine zone.
     *
     * @throws SolverException
     */
    @Test
    public void testWithNoSolution2() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3).run(n1, vm1).run(n2, vm2, vm3).run(n3, vm4).get();
        Set<Node> ns = new HashSet<>(Arrays.asList(n1, n2));
        Quarantine q = new Quarantine(ns);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(q);
        cstrs.add(new Fence(Collections.singleton(vm1), Collections.singleton(n2)));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNull(p);
    }
}
