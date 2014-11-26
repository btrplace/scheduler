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
import org.btrplace.model.constraint.Online;
import org.btrplace.model.constraint.Root;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link CRoot}.
 *
 * @author Fabien Hermenier
 */
public class CRootTest {

    @Test
    public void testBasic() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2).run(n1, vm1, vm2).ready(vm3).get();

        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.doRepair(false);
        Root r1 = new Root(vm1);
        List<SatConstraint> l = new ArrayList<>();
        l.add(r1);
        l.addAll(Online.newOnline(map.getAllNodes()));
        ReconfigurationPlan p = cra.solve(mo, l);
        Assert.assertNotNull(p);
        Model res = p.getResult();
        Assert.assertEquals(n1, res.getMapping().getVMLocation(vm1));
    }
}
