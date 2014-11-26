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

package org.btrplace.scheduler.choco.runner.single;

import org.btrplace.scheduler.choco.DefaultParameters;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.runner.SolutionStatistics;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;

/**
 * Unit tests for {@link org.btrplace.scheduler.choco.runner.single.SingleRunnerStatistics}.
 *
 * @author Fabien Hermenier
 */
public class SingleRunnerStatisticsTest {

    @Test
    public void testInstantiate() {
        Parameters params = new DefaultParameters();
        SingleRunnerStatistics st = new SingleRunnerStatistics(params, 10, 20, 44, 40, 12, 100, 1, 2, false, 7, 34);
        Assert.assertEquals(st.getNbNodes(), 10);
        Assert.assertEquals(st.getNbVMs(), 20);
        Assert.assertEquals(st.getNbConstraints(), 44);
        Assert.assertEquals(st.getParameters(), params);
        Assert.assertEquals(st.getNbManagedVMs(), 40);
        Assert.assertEquals(st.getStart(), 12);
        Assert.assertEquals(st.getSolvingDuration(), 100);
        Assert.assertEquals(st.getNbSearchNodes(), 1);
        Assert.assertEquals(st.getNbBacktracks(), 2);
        Assert.assertFalse(st.hitTimeout());
        Assert.assertEquals(st.getCoreRPBuildDuration(), 7);
        Assert.assertEquals(st.getSpeRPDuration(), 34);
        System.out.println(st);
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testAddSolution() {
        Parameters params = new DefaultParameters();
        SingleRunnerStatistics st = new SingleRunnerStatistics(params, 10, 20, 44, 40, 12, 100, 1, 2, false, 7, 34);
        SolutionStatistics s0 = new SolutionStatistics(1, 2, 3, 4);
        SolutionStatistics s1 = new SolutionStatistics(2, 2, 3, 4);
        SolutionStatistics s2 = new SolutionStatistics(2, 3, 4, 3);
        SolutionStatistics s3 = new SolutionStatistics(2, 4, 5, 2);
        st.addSolution(s1);
        st.addSolution(s2);
        st.addSolution(s3);
        st.addSolution(s0);
        Assert.assertNotNull(st.toString());
        Assert.assertEquals(st.getSolutions().size(), 4);
        Iterator<SolutionStatistics> ite = st.getSolutions().iterator();
        Assert.assertEquals(ite.next(), s1);
        Assert.assertEquals(ite.next(), s2);
        Assert.assertEquals(ite.next(), s3);
        Assert.assertEquals(ite.next(), s0);

    }
}
