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

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.constraint.Online;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * Unit tests for {@link COnline}.
 *
 * @author Fabien Hermenier
 */
public class COnlineTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        Node n1 = mo.newNode();
        Online on = new Online(n1);
        COnline con = new COnline(on);
        Assert.assertEquals(con.toString(), on.toString());
    }

    @Test
    public void testSolvableProblem() throws SchedulerException {
        Model mo = new DefaultModel();
        Node n1 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).off(n1).get();
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, Collections.<SatConstraint>singleton(new Online(n1)));
        Assert.assertNotNull(plan);
        Model res = plan.getResult();
        Assert.assertTrue(res.getMapping().isOnline(n1));
    }
}
