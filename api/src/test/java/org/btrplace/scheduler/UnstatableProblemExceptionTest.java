/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link UnstatableProblemException}.
 */
public class UnstatableProblemExceptionTest {

  @Test
  public void test() {
    final Model mo = new DefaultModel();
    final UnstatableProblemException ex = new UnstatableProblemException(mo, 7);
    Assert.assertEquals(ex.getModel(), mo);
    Assert.assertEquals(ex.timeout(), 7);
    Assert.assertTrue(ex.getMessage().contains("Unable to state about the problem feasibility within the allotted 7 seconds"));
  }
}
