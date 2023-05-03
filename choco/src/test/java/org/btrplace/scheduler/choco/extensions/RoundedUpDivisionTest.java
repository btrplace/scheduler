/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions;


import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class RoundedUpDivisionTest {

    @Test
    public void test1() {
        Model s = new Model();
        IntVar a = s.intVar("a", 0, 5, true);
        IntVar b = s.intVar("b", 0, 5, true);
        double q = 1;
        s.post(new RoundedUpDivision(a, b, q));
        Assert.assertEquals(s.getSolver().findAllSolutions().size(), 6);
    }

    @Test
    public void test2() {
        Model s = new Model();
        IntVar a = s.intVar("a", 0, 32, true);
        IntVar b = s.intVar("b", 0, 48, true);
        double q = 1.5;
        s.post(new RoundedUpDivision(a, b, q));
        Assert.assertEquals(s.getSolver().findAllSolutions().size(), 49);
    }
}
