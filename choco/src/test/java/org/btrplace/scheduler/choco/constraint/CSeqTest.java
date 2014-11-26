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
import org.btrplace.model.constraint.*;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.MappingFiller;
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
public class CSeqTest {

    @Test
    public void testWithOnlyTransitions() throws SchedulerException {
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
        cstrs.addAll(Online.newOnline(map.getAllNodes()));
        ChocoScheduler cra = new DefaultChocoScheduler();
        List<VM> seq = Arrays.asList(vm1, vm2, vm3, vm4);
        cstrs.add(new Seq(seq));
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
    }

    @Test
    public void testWithVMsWithNoTransitions() throws SchedulerException {
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
        ChocoScheduler cra = new DefaultChocoScheduler();
        List<VM> seq = Arrays.asList(vm1, vm2, vm3, vm4);
        cstrs.add(new Seq(seq));
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
        //System.out.println(plan);
    }
}
