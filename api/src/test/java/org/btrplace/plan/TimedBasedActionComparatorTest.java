/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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

package org.btrplace.plan;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.plan.event.Action;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * Unit tests for {@link TimedBasedActionComparator}.
 *
 * @author Fabien Hermenier
 */
public class TimedBasedActionComparatorTest {

    private static TimedBasedActionComparator startCmp = new TimedBasedActionComparator();
    private static TimedBasedActionComparator stopCmp = new TimedBasedActionComparator(false, false);

    private static Random rnd = new Random();

    private static Model mo = new DefaultModel();
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

    /**
     * 0:2 1:3 3:7 1:5 2:3 0:3
     *
     * source Model
     * s(0:2)
     * s(0:3)
     *
     * s(1:3)
     * s(1:5)
     *
     * e(0:2)
     *
     * e(0:3)
     * e(2:3)
     * e(1:3)
     * s(3:7)
     *
     * e(1:5)
     * e(1:7)
     * endsWith(dstModel)
     */
}
