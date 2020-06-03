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
public class AndTest {

    @Test
    public void testInstantiation() {
        And a = new And(Proposition.False, Proposition.True);
        Assert.assertEquals(a.first(), Proposition.False);
        Assert.assertEquals(a.second(), Proposition.True);
    }

    @DataProvider(name = "input")
    public Object[][] getInputs() {
        return new Object[][]{
                {Proposition.True, Proposition.True, Boolean.TRUE},
                {Proposition.True, Proposition.False, Boolean.FALSE},
                {Proposition.False, Proposition.True, Boolean.FALSE},
                {Proposition.False, Proposition.False, Boolean.FALSE},
        };
    }

    @Test(dataProvider = "input")
    public void testTruthTable(Proposition a, Proposition b, Boolean r) {
        And p = new And(a, b);
        Assert.assertEquals(p.eval(new Context()), r);
    }

    @Test
    public void testNot() {
        And and = new And(Proposition.True, Proposition.False);
        Or o = and.not();
        Assert.assertEquals(o.first(), Proposition.False);
        Assert.assertEquals(o.second(), Proposition.True);
    }
}
