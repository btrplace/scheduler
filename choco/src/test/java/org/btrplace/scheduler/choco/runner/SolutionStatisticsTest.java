/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco.runner;

import org.btrplace.model.DefaultModel;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.measure.MeasuresRecorder;
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
        Solver s = new Solver();
        IMeasures m = new MeasuresRecorder(s);
        ReconfigurationPlan p = new DefaultReconfigurationPlan(new DefaultModel());
        SolutionStatistics st = new SolutionStatistics(m, p);
        Assert.assertEquals(st.getMeasures(), m);
        Assert.assertEquals(st.getReconfigurationPlan(), p);
        System.out.println(st);
    }
}
