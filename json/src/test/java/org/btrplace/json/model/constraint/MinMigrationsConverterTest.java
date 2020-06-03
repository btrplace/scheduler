/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.MinMigrations;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link MinMigrationsConverter}.
 *
 * @author Fabien Hermenier
 */
public class MinMigrationsConverterTest {

  @Test
  public void test() throws JSONConverterException {
    Model mo = new DefaultModel();
    ConstraintsConverter conv = new ConstraintsConverter();
    conv.register(new MinMigrationsConverter());

    MinMigrations m = new MinMigrations();
    Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(m)), m);
    System.out.println(conv.toJSON(m));
  }

  @Test
  public void testBundle() {
    Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(MinMigrations.class));
    Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new MinMigrationsConverter().getJSONId()));
  }

}
