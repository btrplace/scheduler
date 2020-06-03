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
public class MaxTestsTest {

    @Test
    public void test() {
        MaxTests m = new MaxTests(3);
        Assert.assertEquals(m.test(Result.CRASH), true);
        Assert.assertEquals(m.test(Result.SUCCESS), true);
        Assert.assertEquals(m.test(Result.UNDER_FILTERING), false);
        Assert.assertEquals(m.test(Result.OVER_FILTERING), false);
        Assert.assertEquals(m.test(Result.SUCCESS), false);
    }

}