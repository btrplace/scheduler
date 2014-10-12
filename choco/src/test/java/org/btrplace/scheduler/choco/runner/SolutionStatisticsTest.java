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

package org.btrplace.scheduler.choco.runner;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Simple unit tests for {@link org.btrplace.scheduler.choco.runner.SolutionStatistics}.
 *
 * @author Fabien Hermenier
 */
public class SolutionStatisticsTest {

    @Test
    public void testInstantiateSatisfaction() {
        SolutionStatistics st = new SolutionStatistics(1, 2, 3);
        Assert.assertEquals(1, st.getNbNodes());
        Assert.assertEquals(2, st.getNbBacktracks());
        Assert.assertEquals(3, st.getTime());
        Assert.assertFalse(st.hasObjective());
    }

    @Test
    public void testInstantiateWithOptimization() {
        SolutionStatistics st = new SolutionStatistics(1, 2, 3, 4);
        Assert.assertEquals(1, st.getNbNodes());
        Assert.assertEquals(2, st.getNbBacktracks());
        Assert.assertEquals(3, st.getTime());
        Assert.assertTrue(st.hasObjective());
        Assert.assertEquals(4, st.getOptValue());
    }

}
