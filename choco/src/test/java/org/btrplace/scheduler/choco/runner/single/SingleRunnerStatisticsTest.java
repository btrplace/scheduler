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

package org.btrplace.scheduler.choco.runner.single;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.DefaultParameters;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.runner.SolutionStatistics;
import org.chocosolver.solver.search.measure.MeasuresRecorder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link org.btrplace.scheduler.choco.runner.single.SingleRunnerStatistics}.
 *
 * @author Fabien Hermenier
 */
public class SingleRunnerStatisticsTest {

    @Test
    public void testInstantiate() {
        Parameters ps = new DefaultParameters();
        Model mo = new DefaultModel();
        long st = System.currentTimeMillis();
        List<SatConstraint> cstrs = new ArrayList<>();
        Instance i = new Instance(mo, cstrs, new MinMTTR());
        SingleRunnerStatistics stats = new SingleRunnerStatistics(ps, i, st);
        Assert.assertEquals(stats.getStart(), st);
        Assert.assertEquals(stats.getCoreBuildDuration(), -1);
        Assert.assertEquals(stats.getSpecializationDuration(), -1);
        Assert.assertEquals(stats.getInstance(), i);
        Assert.assertEquals(stats.getNbManagedVMs(), -1);
        Assert.assertEquals(stats.getParameters(), ps);
        Assert.assertEquals(stats.getSolutions().size(), 0);
        Assert.assertEquals(stats.completed(), false);
        Assert.assertEquals(stats.getMeasures(), null);

        stats.setCoreBuildDuration(12);
        stats.setSpecialisationDuration(17);
        stats.setNbManagedVMs(18);
        stats.setCompleted(true);

        Assert.assertEquals(stats.getCoreBuildDuration(), 12);
        Assert.assertEquals(stats.getSpecializationDuration(), 17);
        Assert.assertEquals(stats.getNbManagedVMs(), 18);
        Assert.assertEquals(stats.completed(), true);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        SolutionStatistics sol = new SolutionStatistics(new MeasuresRecorder(""), plan);
        stats.addSolution(sol);
        Assert.assertEquals(stats.getSolutions().size(), 1);
        Assert.assertEquals(stats.getSolutions().get(0), sol);
    }
}
