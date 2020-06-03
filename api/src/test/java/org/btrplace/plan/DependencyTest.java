/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.plan.event.Action;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple unit tests for {@link Dependency}.
 *
 * @author Fabien Hermenier
 */
public class DependencyTest {

  private static final Model mo = new DefaultModel();
  VM vm = mo.newVM();

    @Test
    public void testInstantiation() {
        Action a = new MockAction(vm, 1, 4);
        Set<Action> d = new HashSet<>();
        d.add(new MockAction(vm, 2, 5));
        d.add(new MockAction(vm, 3, 7));
        Dependency dep = new Dependency(a, d);
        Assert.assertEquals(dep.getAction(), a);
        Assert.assertEquals(dep.getDependencies(), d);
        Assert.assertFalse(dep.toString().contains("null"));
    }
}
