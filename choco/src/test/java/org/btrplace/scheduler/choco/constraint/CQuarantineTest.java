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
import org.btrplace.model.constraint.Fence;
import org.btrplace.model.constraint.Quarantine;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link CQuarantine}.
 *
 * @author Fabien Hermenier
 */
public class CQuarantineTest {

    @Test
    public void testWithSatisfiedModel() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3).run(n1, vm1).run(n2, vm2, vm3).run(n3, vm4).get();
        Quarantine q = new Quarantine(n2);
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>singleton(q));
        Assert.assertNotNull(p);
    }

    /**
     * A VM try to come into the quarantine zone.
     *
     * @throws org.btrplace.scheduler.SchedulerException
     */
    @Test
    public void testWithNoSolution1() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3).run(n1, vm1).run(n2, vm2, vm3).run(n3, vm4).get();
        Quarantine q = new Quarantine(n1);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(q);
        cstrs.add(new Fence(vm4, Collections.singleton(n1)));
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNull(p);
    }

    /**
     * A VM try to leave the quarantine zone.
     *
     * @throws org.btrplace.scheduler.SchedulerException
     */
    @Test
    public void testWithNoSolution2() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2, n3).run(n1, vm1).run(n2, vm2, vm3).run(n3, vm4).get();
        Quarantine q = new Quarantine(n2);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(q);
        cstrs.add(new Fence(vm1, Collections.singleton(n2)));
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNull(p);
    }
}
