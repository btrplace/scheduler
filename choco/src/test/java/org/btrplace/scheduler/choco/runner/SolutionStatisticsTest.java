/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.runner;

import org.btrplace.model.DefaultModel;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Simple unit tests for {@link org.btrplace.scheduler.choco.runner.SolutionStatistics}.
 *
 * @author Fabien Hermenier
 */
public class SolutionStatisticsTest {

    @Test
    public void test() {
        Metrics m = new Metrics();
        ReconfigurationPlan p = new DefaultReconfigurationPlan(new DefaultModel());
        SolutionStatistics st = new SolutionStatistics(m, p);
        Assert.assertFalse(st.hasObjective());
        st.setObjective(12);
        Assert.assertEquals(st.getMetrics(), m);
        Assert.assertEquals(st.getReconfigurationPlan(), p);
        Assert.assertTrue(st.hasObjective());
        Assert.assertEquals(st.objective(), 12);
        System.out.println(st);
    }
}
