/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.spec.term.IntMinus;
import org.btrplace.safeplace.spec.term.Minus;
import org.btrplace.safeplace.spec.term.SetMinus;
import org.btrplace.safeplace.spec.type.IntType;
import org.btrplace.safeplace.spec.type.SetType;
import org.btrplace.safeplace.spec.type.VMStateType;
import org.btrplace.safeplace.testing.verification.spec.Context;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class MinusTest {

    @Test
    public void testInts() {
        Minus<?> p = new IntMinus(IntType.getInstance().parse("5"), IntType.getInstance().parse("7"));
        Assert.assertEquals(p.eval(new Context()), -2);
        Assert.assertEquals(p.type(), IntType.getInstance());
    }

    @Test
    public void testCollections() {
        Constant v1 = new Constant(Arrays.asList(1, 2), new SetType(IntType.getInstance()));
        Constant v2 = new Constant(Arrays.asList(2, 5), new SetType(IntType.getInstance()));
        Minus<?> p = new SetMinus(v1, v2);
        Set<?> s = (Set<?>) p.eval(new Context());
        Assert.assertEquals(s.size(), 1);
        Assert.assertEquals(p.type(), new SetType(IntType.getInstance()));
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testBadCollections() throws RuntimeException {
        Constant v1 = new Constant(Arrays.asList(1, 2), new SetType(IntType.getInstance()));
        Constant v2 = new Constant(Collections.singletonList(VMStateType.getInstance().parse("running")), new SetType(VMStateType.getInstance()));
        @SuppressWarnings("unused")
        SetMinus bad = new SetMinus(v1, v2);
    }

    @Test
    public void testMinusMinus() {
        Minus<Integer> p1 = new IntMinus(IntType.getInstance().parse("5"), IntType.getInstance().parse("7"));
        Minus<Integer> p2 = new IntMinus(IntType.getInstance().parse("1"), IntType.getInstance().parse("2"));
        Minus<?> p3 = new IntMinus(p1, p2);
        Assert.assertEquals(p3.eval(new Context()), -1);
    }

}
