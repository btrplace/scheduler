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

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.*;
import org.btrplace.model.constraint.MaxOnline;
import org.btrplace.model.constraint.Online;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CMaxOnline}.
 */
public class CMaxOnlineTest {

    @Test
    public void discreteMaxOnlineTest() throws SchedulerException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        Mapping map = new MappingFiller().on(n1, n2, n3)
                .run(n1, vm1)
                .run(n2, vm2)
                .run(n3, vm3).get();
        MappingUtils.fill(map, model.getMapping());
        Set<Node> nodes = map.getAllNodes();
        MaxOnline maxon = new MaxOnline(nodes, 1);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.setMaxEnd(3);
        //cra.setTimeLimit(3);
        ReconfigurationPlan plan = cra.solve(model, Collections.<SatConstraint>singleton(maxon));
        Assert.assertTrue(maxon.isSatisfied(plan.getResult()));
    }

    @Test
    public void discreteMaxOnlineTest2() throws SchedulerException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        ShareableResource resources = new ShareableResource("vcpu", 1, 1);
        resources.setCapacity(n1, 4);
        resources.setCapacity(n2, 8);
        resources.setCapacity(n3, 2);
        resources.setConsumption(vm4, 2);
        Mapping map = new MappingFiller().on(n1, n2, n3)
                .run(n1, vm1, vm4)
                .run(n2, vm2)
                .run(n3, vm3).get();
        model.attach(resources);
        MappingUtils.fill(map, model.getMapping());
        Set<Node> nodes = map.getAllNodes();
        MaxOnline maxon = new MaxOnline(nodes, 2);
        Set<Node> nodes2 = new HashSet<Node>(Arrays.asList(n1, n2));

        MaxOnline maxon2 = new MaxOnline(nodes2, 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxon);
        constraints.add(maxon2);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.setMaxEnd(4);
        cra.getConstraintMapper().register(new CMaxOnline.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertTrue(maxon.isSatisfied(plan.getResult()));
    }

    @Test
    public void testSimpleContinuousCase() throws SchedulerException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        model.getMapping().addOnlineNode(n1);
        model.getMapping().addOfflineNode(n2);
        MaxOnline maxOnline = new MaxOnline(model.getMapping().getAllNodes(), 1, true);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxOnline);
        constraints.add(new Online(n2));
        ChocoScheduler cra = new DefaultChocoScheduler();
        //cra.setTimeLimit(5);
        cra.setMaxEnd(4);
        cra.getConstraintMapper().register(new CMaxOnline.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        Assert.assertTrue(maxOnline.isSatisfied(plan));
    }

    @Test
    public void testContinuousRestrictionSimpleCase() throws SchedulerException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        ShareableResource resources = new ShareableResource("cpu", 4, 1);
        resources.setCapacity(n1, 8);
        resources.setConsumption(vm4, 2);
        Mapping map = new MappingFiller().on(n1, n3).off(n2)
                .run(n1, vm1, vm4)
                .run(n3, vm3).get();
        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);
        MaxOnline maxon = new MaxOnline(map.getAllNodes(), 2, true);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxon);
        constraints.add(new Online(n2));
        ChocoScheduler cra = new DefaultChocoScheduler();
        //cra.setTimeLimit(3);
        cra.setMaxEnd(3);
        cra.getConstraintMapper().register(new CMaxOnline.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        Assert.assertTrue(maxon.isSatisfied(plan));
    }

    @Test
    public void complexContinuousTest2() throws SchedulerException {
        Model model = new DefaultModel();
        Node n1 = model.newNode(1);
        Node n2 = model.newNode(2);
        Node n3 = model.newNode(3);
        Node n4 = model.newNode(4);
        Node n5 = model.newNode(5);
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        ShareableResource resources = new ShareableResource("cpu", 2, 1);
        resources.setCapacity(n1, 4);
        resources.setCapacity(n2, 8);
        resources.setConsumption(vm4, 2);
        Mapping map = new MappingFiller().on(n1, n2, n3).off(n4, n5)
                .run(n1, vm1, vm4)
                .run(n2, vm2)
                .run(n3, vm3).get();
        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);

        MaxOnline maxOn = new MaxOnline(map.getAllNodes(), 4, true);
        MaxOnline maxOn2 = new MaxOnline(new HashSet<Node>(Arrays.asList(n2, n3, n4)), 2, true);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxOn);
        constraints.add(maxOn2);
        constraints.addAll(Online.newOnline(Arrays.asList(n4, n5)));

        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.setTimeLimit(3);
        cra.setMaxEnd(10);
        cra.getConstraintMapper().register(new CMaxOnline.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        Assert.assertTrue(maxOn.isSatisfied(plan));
        System.out.println(plan);
    }
}
