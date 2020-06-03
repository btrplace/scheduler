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
public class ImpliesTest {

    @DataProvider(name = "input")
    public Object[][] getInputs() {
        return new Object[][]{
                {Proposition.True, Proposition.True, Boolean.TRUE},
                {Proposition.True, Proposition.False, Boolean.FALSE},
                {Proposition.False, Proposition.True, Boolean.TRUE},
                {Proposition.False, Proposition.False, Boolean.TRUE},
        };
    }

    @Test(dataProvider = "input")
    public void testTruthTable(Proposition a, Proposition b, Boolean r) {
        Implies p = new Implies(a, b);
        Assert.assertEquals(p.eval(new Context()), r);
    }

    @Test
    public void testNot() {
        Implies p = new Implies(Proposition.True, Proposition.False); //not(or(not(a),b)) -> and(a, not(b))
        And a = p.not();
        Assert.assertEquals(a.first(), Proposition.True);
        Assert.assertEquals(a.second(), Proposition.True);
    }
}
