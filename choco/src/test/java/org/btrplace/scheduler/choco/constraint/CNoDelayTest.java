/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Mapping;
import org.btrplace.model.MappingUtils;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Ban;
import org.btrplace.model.constraint.MaxOnline;
import org.btrplace.model.constraint.NoDelay;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link org.btrplace.scheduler.choco.constraint.CNoDelay}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.scheduler.choco.constraint.CNoDelay
 */
public class CNoDelayTest {

    @Test
    public void testOk1() throws SchedulerException {
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

        Mapping map = model.getMapping().on(n1, n2, n3)
                .run(n1, vm1)
                .run(n2, vm2)
                .run(n3, vm3)
                .run(n3, vm4);

        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);

        Ban b = new Ban(vm1, Collections.singleton(n1));
        NoDelay nd = new NoDelay(vm3);

        // 1 solution (priority to vm3): vm3 to n2 ; vm4 to n2 ; vm1 to n3

        List<SatConstraint> constraints = new ArrayList<>();
        constraints.add(nd);
        constraints.add(b);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.getMapper().mapConstraint(MaxOnline.class, CMaxOnline.class);
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        Assert.assertTrue(nd.isSatisfied(plan));
    }

    @Test
    public void testOk2() throws SchedulerException {
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

        Mapping map = model.getMapping().on(n1, n2, n3)
                .run(n1, vm1)
                .run(n2, vm2)
                .run(n3, vm3)
                .run(n3, vm4);

        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);

        Ban b = new Ban(vm1, Collections.singleton(n1));
        NoDelay nd = new NoDelay(vm4);

        // 1 solution (priority to vm4): vm4 to n2 ; vm3 to n2 ; vm1 to n3

        List<SatConstraint> constraints = new ArrayList<>();
        constraints.add(nd);
        constraints.add(b);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.getMapper().mapConstraint(MaxOnline.class, CMaxOnline.class);
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        Assert.assertTrue(nd.isSatisfied(plan));
    }

    @Test
    public void testKo() throws SchedulerException {
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

        Mapping map = model.getMapping().on(n1, n2, n3)
                .run(n1, vm1)
                .run(n2, vm2)
                .run(n3, vm3)
                .run(n3, vm4);

        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);

        Ban b = new Ban(vm1, Collections.singleton(n1));
        NoDelay nd = new NoDelay(vm1);

        // No solution: unable to migrate vm1 at t=0

        List<SatConstraint> constraints = new ArrayList<>();
        constraints.add(nd);
        constraints.add(b);
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.getMapper().mapConstraint(MaxOnline.class, CMaxOnline.class);
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNull(plan);
    }
}
