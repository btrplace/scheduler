/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing.limit;

import org.btrplace.safeplace.testing.Result;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class LimitsTest {

    @Test
    public void testSimple() {
        Limits l = new Limits();
        l.failures(1);
        Assert.assertFalse(l.test(Result.CRASH));

        //Override
        l.failures(3);
        Assert.assertTrue(l.test(Result.CRASH));

        l.tests(1);
        Assert.assertFalse(l.test(Result.SUCCESS));//because of maxtests

        l.clear();
        Assert.assertTrue(l.test(Result.CRASH));
    }
}