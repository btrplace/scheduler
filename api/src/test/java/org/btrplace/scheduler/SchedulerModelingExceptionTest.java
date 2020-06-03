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
 * Unit tests for {@link SchedulerModelingException}.
 */
public class SchedulerModelingExceptionTest {

  @Test
  public void simpleTests() {

    final Model mo = new DefaultModel();

    // The missing view helper.
    SchedulerModelingException ex = SchedulerModelingException.missingView(mo, "foo");
    Assert.assertEquals(ex.getModel(), mo);
    Assert.assertTrue(ex.getMessage().contains("foo"));

    // The full constructor.
    final Exception baz = new Exception("baz");
    ex = new SchedulerModelingException(mo, "foo", baz);
    Assert.assertEquals(ex.getModel(), mo);
    Assert.assertEquals(ex.getMessage(), "foo");
    Assert.assertEquals(ex.getCause(), baz);
  }

}
