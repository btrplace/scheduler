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

package org.btrplace.scheduler.choco.runner.disjoint;

import org.btrplace.scheduler.choco.DefaultParameters;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.runner.SolutionStatistics;
import org.btrplace.scheduler.choco.runner.single.SingleRunnerStatistics;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link StaticPartitioningStatistics}.
 *
 * @author Fabien Hermenier
 */
public class StaticPartitioningStatisticsTest {

    @Test
    public void test() {
        Parameters ps = new DefaultParameters();
        StaticPartitioningStatistics stats = new StaticPartitioningStatistics(ps,
                7, 8, 9, 4, 5, 6, 2, 3);
        Assert.assertEquals(stats.getNbNodes(), 7);
        Assert.assertEquals(stats.getNbVMs(), 8);
        Assert.assertEquals(stats.getNbConstraints(), 9);
        Assert.assertEquals(stats.getStart(), 4);
        Assert.assertEquals(stats.getSplitDuration(), 5);
        Assert.assertEquals(stats.getSolvingDuration(), 6);
        Assert.assertEquals(stats.getNbWorkers(), 2);
        Assert.assertEquals(stats.getNbParts(), 3);

        //Add some partitions results
        SingleRunnerStatistics statsP1 = new SingleRunnerStatistics(ps, 2, 3, 3, 1, 12, 2, 100, 200, false, 15, 20);
        SingleRunnerStatistics statsP2 = new SingleRunnerStatistics(ps, 2, 2, 2, 1, 12, 2, 70, 200, false, 45, 30);
        SingleRunnerStatistics statsP3 = new SingleRunnerStatistics(ps, 3, 3, 4, 2, 230, 2, 50, 200, true, 57, 20);

        stats.addPartitionStatistics(statsP1);
        stats.addPartitionStatistics(statsP2);
        Assert.assertFalse(stats.hitTimeout());
        Assert.assertEquals(stats.getSolutions().size(), 0);
        stats.addPartitionStatistics(statsP3);

        Assert.assertTrue(stats.hitTimeout());
        Assert.assertEquals(stats.getNbSearchNodes(), 220);
        Assert.assertEquals(stats.getNbBacktracks(), 600);
        Assert.assertEquals(stats.getNbManagedVMs(), 4);
        Assert.assertEquals(stats.getCoreRPBuildDuration(), 57);
        Assert.assertEquals(stats.getSpeRPDuration(), 30);

        statsP1.addSolution(new SolutionStatistics(1, 2, 3, 5));
        statsP2.addSolution(new SolutionStatistics(6, 7, 8, 9));
        Assert.assertEquals(stats.getSolutions().size(), 0);
        statsP3.addSolution(new SolutionStatistics(10, 11, 12, 13));
        Assert.assertEquals(stats.getSolutions().size(), 1);
        SolutionStatistics sol = stats.getSolutions().get(0);
        Assert.assertEquals(sol.getNbNodes(), 17);
        Assert.assertEquals(sol.getNbBacktracks(), 20);
        Assert.assertEquals(sol.getTime(), 238);             //start = lastPartition(start = 230 + 12) - start (4)-> 238
        Assert.assertEquals(sol.getOptValue(), 27);

        //A new solution
        statsP3.addSolution(new SolutionStatistics(14, 15, 16, 17));
        Assert.assertEquals(stats.getSolutions().size(), 2);
        sol = stats.getSolutions().get(1);
        Assert.assertEquals(sol.getNbNodes(), 21);
        Assert.assertEquals(sol.getNbBacktracks(), 24);
        Assert.assertEquals(sol.getTime(), 242);             //start = lastPartition(start = 230 + 16) - start (4)-> 242
        Assert.assertEquals(sol.getOptValue(), 31);
        System.out.println(stats);
        System.out.flush();
    }
}
