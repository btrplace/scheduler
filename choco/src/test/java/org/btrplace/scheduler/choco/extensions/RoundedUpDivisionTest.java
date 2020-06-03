/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
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
        Assert.assertEquals(6, s.getSolver().findAllSolutions().size());
        //Assert.assertEquals(s.getNbSolutions(), 6);
    }

    @Test
    public void test2() {
        Model s = new Model();
        IntVar a = s.intVar("a", 0, 32, true);
        IntVar b = s.intVar("b", 0, 48, true);
        double q = 1.5;
        s.post(new RoundedUpDivision(a, b, q));
        Assert.assertEquals(s.getSolver().findAllSolutions().size(), 49);
        //Assert.assertEquals(s.getNbSolutions(), 33);
    }
    /*
    private static void pretty(int a, int b, double q) {
        StringBuilder a1 = new StringBuilder();
        for (int i = 0; i < b; i++) {
            a1.append("(").append(i).append(",").append((int)Math.ceil(i / q)).append(") ");
        }
        System.err.println(a1.toString());
    } */
}
