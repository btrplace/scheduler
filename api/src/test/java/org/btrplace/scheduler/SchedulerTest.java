/*
 * Copyright (c) 2019 University Nice Sophia Antipolis
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

package org.btrplace.scheduler;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.constraint.MinMigrations;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link Scheduler}.
 */
public class SchedulerTest {

  @Test
  public void testDefaultMethods() {
    final ReconfigurationPlan expected = new DefaultReconfigurationPlan(new DefaultModel());
    final Scheduler sched = i -> expected;
    final List<SatConstraint> cstrs = new ArrayList<>();
    final MinMigrations obj = new MinMigrations();
    Assert.assertEquals(expected, sched.solve(expected.getOrigin(), cstrs, obj));
    Assert.assertEquals(expected, sched.solve(expected.getOrigin(), cstrs));
  }
}
