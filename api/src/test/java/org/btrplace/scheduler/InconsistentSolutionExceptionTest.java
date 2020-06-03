/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link InconsistentSolutionException}.
 */
public class InconsistentSolutionExceptionTest {

  @Test
  public void test() {
    final Model mo = new DefaultModel();
    final ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
    final InconsistentSolutionException ex = new InconsistentSolutionException(plan, "foo");
    Assert.assertEquals(ex.getModel(), mo);
    Assert.assertEquals(ex.getResult(), plan);
    Assert.assertEquals(ex.getMessage(), "foo");
  }
}
