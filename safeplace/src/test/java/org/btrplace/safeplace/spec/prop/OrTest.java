/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.testing.verification.spec.Context;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class OrTest {

    @Test
    public void testInstantiation() {
        Or a = new Or(Proposition.False, Proposition.True);
        Assert.assertEquals(a.first(), Proposition.False);
        Assert.assertEquals(a.second(), Proposition.True);
    }

    @DataProvider(name = "input")
    public Object[][] getInputs() {
        return new Object[][]{
                {Proposition.True, Proposition.True, Boolean.TRUE},
                {Proposition.True, Proposition.False, Boolean.TRUE},
                {Proposition.False, Proposition.True, Boolean.TRUE},
                {Proposition.False, Proposition.False, Boolean.FALSE},
        };
    }

    @Test(dataProvider = "input")
    public void testTruthTable(Proposition a, Proposition b, Boolean r) {
        Or p = new Or(a, b);
        Assert.assertEquals(p.eval(new Context()), r);
    }

    @Test
    public void testNot() {
        Or or = new Or(Proposition.True, Proposition.False);
        And o = or.not();
        Assert.assertEquals(o.first(), Proposition.False);
        Assert.assertEquals(o.second(), Proposition.True);
    }
}
