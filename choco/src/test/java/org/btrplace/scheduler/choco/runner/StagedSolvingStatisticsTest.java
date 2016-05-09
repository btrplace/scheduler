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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link StagedSolvingStatistics}.
 *
 * @author Fabien Hermenier
 */
public class StagedSolvingStatisticsTest {

    @Test
    public void testEmpty() {
        StagedSolvingStatistics stats = new StagedSolvingStatistics();
        Assert.assertEquals(stats.getSolutions().size(), 0);
        Assert.assertEquals(stats.getMeasures(), null);
        Assert.assertEquals(stats.getNbManagedVMs(), 0);
        Assert.assertEquals(stats.getStart(), -1);
        Assert.assertEquals(stats.getInstance(), null);
        Assert.assertEquals(stats.getNbStages(), 0);
        Assert.assertEquals(stats.getCoreBuildDuration(), -1);
        Assert.assertEquals(stats.getSpecializationDuration(), -1);
        Assert.assertEquals(stats.completed(), false);
    }
}