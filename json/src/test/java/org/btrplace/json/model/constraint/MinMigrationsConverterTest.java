/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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
