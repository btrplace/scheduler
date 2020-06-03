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

/**
 * Unit tests for {@link TimedBasedActionComparator}.
 *
 * @author Fabien Hermenier
 */
public class TimedBasedActionComparatorTest {

  private static final TimedBasedActionComparator startCmp = new TimedBasedActionComparator();
  private static final TimedBasedActionComparator stopCmp = new TimedBasedActionComparator(false, false);

  private static final Model mo = new DefaultModel();
  VM vm = mo.newVM();

  @Test
  public void testPrecedence() {
    Action a = new MockAction(vm, 0, 4);
    Action b = new MockAction(vm, 4, 10);
    Assert.assertTrue(startCmp.compare(a, b) < 0);
    Assert.assertTrue(startCmp.compare(b, a) > 0);

        Assert.assertTrue(stopCmp.compare(a, b) < 0);
        Assert.assertTrue(stopCmp.compare(b, a) > 0);

    }

    @Test
    public void testEquality() {
        Action a = new MockAction(vm, 0, 4);
        Action b = new MockAction(vm, 0, 4);
        Assert.assertEquals(startCmp.compare(a, b), 0);

        Assert.assertEquals(stopCmp.compare(a, b), 0);
    }

    @Test
    public void testEqualityWithSimultaneousDisallowed() {
        VM vm2 = mo.newVM();
        Action a = new MockAction(vm, 0, 4);
        Action b = new MockAction(vm2, 0, 4);
        Assert.assertNotEquals(new TimedBasedActionComparator(false, true).compare(a, b), 0);
        Assert.assertNotEquals(new TimedBasedActionComparator(true, true).compare(a, b), 0);

    }

    @Test
    public void testOverlap1() {
        Action a = new MockAction(vm, 0, 4);
        Action b = new MockAction(vm, 2, 4);
        Assert.assertTrue(startCmp.compare(a, b) < 0);
        Assert.assertTrue(stopCmp.compare(a, b) < 0);
    }

    @Test
    public void testOverlap2() {
        Action a = new MockAction(vm, 0, 4);
        Action b = new MockAction(vm, 0, 3);
        Assert.assertTrue(startCmp.compare(a, b) > 0);
        Assert.assertTrue(stopCmp.compare(a, b) > 0);
    }
}
