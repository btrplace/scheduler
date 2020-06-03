/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.constraint.Online;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
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
        mo.getMapping().off(n1);
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan plan = cra.solve(mo, Collections.singleton(new Online(n1)));
        Assert.assertNotNull(plan);
        Model res = plan.getResult();
        Assert.assertTrue(res.getMapping().isOnline(n1));
    }
}
