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
