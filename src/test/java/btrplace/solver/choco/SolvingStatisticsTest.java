/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco;

import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;

/**
 * Unit tests for {@link SolvingStatistics}.
 *
 * @author Fabien Hermenier
 */
public class SolvingStatisticsTest {

    @Test
    public void testInstantiate() {
        SolvingStatistics st = new SolvingStatistics(1, 2, 3, false);
        Assert.assertEquals(1, st.getTime());
        Assert.assertEquals(2, st.getNbNodes());
        Assert.assertEquals(3, st.getNbBacktracks());
        Assert.assertFalse(st.isTimeout());
        st = new SolvingStatistics(1, 2, 3, true);
        Assert.assertTrue(st.isTimeout());
        Assert.assertNotNull(st.toString());
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testAddSolution() {
        SolvingStatistics st = new SolvingStatistics(1, 2, 3, false);
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
        Assert.assertEquals(ite.next(), s0);
        Assert.assertEquals(ite.next(), s1);
        Assert.assertEquals(ite.next(), s2);
        Assert.assertEquals(ite.next(), s3);

    }
}
