/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
        s.setMetrics(new Metrics(mr));
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

        SingleRunnerStatistics s1 = new SingleRunnerStatistics(ps, i, st);

        s1.setCoreBuildDuration(2);
        s1.setSpecialisationDuration(3);
        s1.setNbManagedVMs(7);
        Metrics r1 = new Metrics(0, 3, 12, 7, 9, 8);
        s1.setMetrics(r1);
        s1.addSolution(new SolutionStatistics(r1, p));
        StagedSolvingStatistics stats = new StagedSolvingStatistics(s1);

        SingleRunnerStatistics s2 = new SingleRunnerStatistics(ps, i, st);
        s2.setCoreBuildDuration(10);
        s2.setSpecialisationDuration(20);
        s2.setNbManagedVMs(15);
        Metrics r2 = new Metrics(0, 7, 28, 18, 17, 4);
/*        r2.timeCount = 7;
        r2.backtrackCount = 18;
        r2.nodeCount = 28;
        r2.failCount = 17;
        r2.hasObjective = true;
        r2.objectiveOptimal = false;*/
        s2.setMetrics(r2);
        s2.addSolution(new SolutionStatistics(r2, p));

        stats.append(s2);
        Assert.assertEquals(stats.getNbStages(), 2);
        Assert.assertEquals(stats.getCoreBuildDuration(), 12);
        Assert.assertEquals(stats.getSpecializationDuration(), 23);
        Assert.assertEquals(stats.getNbManagedVMs(), 15);
        Assert.assertEquals(stats.getSolutions(), s2.getSolutions());
        Metrics res = stats.getMetrics();
        Assert.assertEquals(res.timeCount(), r1.timeCount() + r2.timeCount());
        Assert.assertEquals(res.backtracks(), r1.backtracks() + r2.backtracks());
        Assert.assertEquals(res.nodes(), r1.nodes() + r2.nodes());
        Assert.assertEquals(res.fails(), r1.fails() + r2.fails());
        Assert.assertFalse(stats.completed());

        Assert.assertEquals(stats.getSolutions().size(), 1);
        Assert.assertEquals(stats.getSolutions().get(0), s2.getSolutions().get(0));
    }
}