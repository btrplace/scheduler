/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
