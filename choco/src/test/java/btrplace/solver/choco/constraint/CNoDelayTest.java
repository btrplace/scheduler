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
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
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
    public void testOk1() throws SolverException {
        Model model = new DefaultModel();

        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();

        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();

        ShareableResource resources = new ShareableResource("cpu", 4, 1);
        resources.setCapacity(n2, 3);
        resources.setConsumption(vm1, 4);

        Mapping map = new MappingFiller().on(n1, n2, n3)
                .run(n1, vm1)
                .run(n2, vm2)
                .run(n3, vm3)
                .run(n3, vm4).get();

        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);

        Ban b = new Ban(vm1, Collections.singleton(n1));
        NoDelay nd = new NoDelay(vm3);

        // 1 solution (priority to vm3): vm3 to n2 ; vm4 to n2 ; vm1 to n3

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(nd);
        constraints.add(b);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getConstraintMapper().register(new CMaxOnline.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        Assert.assertTrue(nd.isSatisfied(plan));
    }

    @Test
    public void testOk2() throws SolverException {
        Model model = new DefaultModel();

        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();

        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();

        ShareableResource resources = new ShareableResource("cpu", 4, 1);
        resources.setCapacity(n2, 3);
        resources.setConsumption(vm1, 4);

        Mapping map = new MappingFiller().on(n1, n2, n3)
                .run(n1, vm1)
                .run(n2, vm2)
                .run(n3, vm3)
                .run(n3, vm4).get();

        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);

        Ban b = new Ban(vm1, Collections.singleton(n1));
        NoDelay nd = new NoDelay(vm4);

        // 1 solution (priority to vm4): vm4 to n2 ; vm3 to n2 ; vm1 to n3

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(nd);
        constraints.add(b);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getConstraintMapper().register(new CMaxOnline.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        Assert.assertTrue(nd.isSatisfied(plan));
    }

    @Test
    public void testKo() throws SolverException {
        Model model = new DefaultModel();

        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();

        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();

        ShareableResource resources = new ShareableResource("cpu", 4, 1);
        resources.setCapacity(n2, 3);
        resources.setConsumption(vm1, 4);

        Mapping map = new MappingFiller().on(n1, n2, n3)
                .run(n1, vm1)
                .run(n2, vm2)
                .run(n3, vm3)
                .run(n3, vm4).get();

        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);

        Ban b = new Ban(vm1, Collections.singleton(n1));
        NoDelay nd = new NoDelay(vm1);

        // No solution: unable to migrate vm1 at t=0

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(nd);
        constraints.add(b);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getConstraintMapper().register(new CMaxOnline.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNull(plan);
    }
}
