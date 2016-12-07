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
import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.DefaultParameters;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.runner.single.SingleRunnerStatistics;
import org.chocosolver.solver.search.measure.MeasuresRecorder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link StagedSolvingStatistics}.
 *
 * @author Fabien Hermenier
 */
public class StagedSolvingStatisticsTest {

    private final static Parameters ps = new DefaultParameters();
    private final static Model mo = new DefaultModel();
    private final static ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
    private final static List<SatConstraint> cstrs = Collections.emptyList();
    private final static Instance i = new Instance(mo, cstrs, new MinMTTR());
    private final static long st = System.currentTimeMillis();

    @Test
    public void testSingle() {
        SingleRunnerStatistics s = new SingleRunnerStatistics(ps, i, st);
        MeasuresRecorder mr = new MeasuresRecorder("");
        s.setMeasures(mr);
        StagedSolvingStatistics stats = new StagedSolvingStatistics(s);
        Assert.assertEquals(stats.getSolutions().size(), 0);
        Assert.assertEquals(stats.getStage(0), s);
        Assert.assertEquals(stats.getNbManagedVMs(), -1);
        Assert.assertEquals(stats.getStart(), st);
        Assert.assertEquals(stats.getInstance(), i);
        Assert.assertEquals(stats.getNbStages(), 1);
        Assert.assertEquals(stats.getCoreBuildDuration(), -1);
        Assert.assertEquals(stats.getSpecializationDuration(), -1);
        Assert.assertEquals(stats.completed(), false);
    }

    @Test
    public void testMultiple() {
        Assert.fail();
    }
    /*
        SingleRunnerStatistics s1 = new SingleRunnerStatistics(ps, i, st);
        s1.setCoreBuildDuration(2);
        s1.setSpecialisationDuration(3);
        s1.setNbManagedVMs(7);
        MeasuresRecorder r1 = new MeasuresRecorder("");
        r1.timeCount = 3;
        r1.nodeCount = 12;
        r1.backtrackCount = 7;
        r1.failCount = 9;
        r1.hasObjective = true;
        r1.objectiveOptimal = true;
        s1.setMeasures(r1);
        s1.addSolution(new SolutionStatistics(r1, p));
        StagedSolvingStatistics stats = new StagedSolvingStatistics(s1);

        SingleRunnerStatistics s2 = new SingleRunnerStatistics(ps, i, st);
        s2.setCoreBuildDuration(10);
        s2.setSpecialisationDuration(20);
        s2.setNbManagedVMs(15);
        MeasuresRecorder r2 = new MeasuresRecorder(new Solver());
        r2.timeCount = 7;
        r2.backtrackCount = 18;
        r2.nodeCount = 28;
        r2.failCount = 17;
        r2.hasObjective = true;
        r2.objectiveOptimal = false;
        s2.setMeasures(r2);
        s2.addSolution(new SolutionStatistics(r2, p));

        stats.append(s2);
        Assert.assertEquals(stats.getNbStages(), 2);
        Assert.assertEquals(stats.getCoreBuildDuration(), 12);
        Assert.assertEquals(stats.getSpecializationDuration(), 23);
        Assert.assertEquals(stats.getNbManagedVMs(), 15);
        Assert.assertEquals(stats.getSolutions(), s2.getSolutions());
        IMeasures res = stats.getMeasures();
        Assert.assertEquals(res.getElapsedTimeInNanoseconds(), r1.getElapsedTimeInNanoseconds() + r2.getElapsedTimeInNanoseconds());
        Assert.assertEquals(res.getBackTrackCount(), r1.getBackTrackCount() + r2.getBackTrackCount());
        Assert.assertEquals(res.getNodeCount(), r1.getNodeCount() + r2.getNodeCount());
        Assert.assertEquals(res.getFailCount(), r1.getFailCount() + r2.getFailCount());
        Assert.assertTrue(res.hasObjective());
        Assert.assertFalse(res.isObjectiveOptimal());
    }*/
}