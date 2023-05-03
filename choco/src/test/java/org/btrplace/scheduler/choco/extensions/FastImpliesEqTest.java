/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/*
 * Created on 18/09/14.
 *
 * @author Sophie Demassey
 */public class FastImpliesEqTest {

    @Test
    public void test1() {
        Model s = new Model();
        //SMF.log(s, true, true);
        BoolVar b = s.boolVar("b");
        IntVar x = s.intVar("x", 0, 3, true);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(s.getSolver().findAllSolutions().size(), 5);
    }

    @Test
    public void test2() {
        Model s = new Model();
        BoolVar b = s.boolVar("b");
        IntVar x = s.intVar("x", 0, 3, false);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(s.getSolver().findAllSolutions().size(), 5);
    }

    @Test
    public void test3() {
        Model s = new Model();
        BoolVar b = s.boolVar("b");
        IntVar x = s.intVar("x", 0, 2, true);
        int c = 3;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(s.getSolver().findAllSolutions().size(), 3);
    }

    @Test
    public void test4() {
        Model s = new Model();
        BoolVar b = s.boolVar(true);
        IntVar x = s.intVar("x", 0, 2, true);
        int c = 3;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(s.getSolver().findAllSolutions().size(), 0);
    }

    @Test
    public void test5() {
        Model s = new Model();
        BoolVar b = s.boolVar(true);
        IntVar x = s.intVar("x", 0, 3, true);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(s.getSolver().findAllSolutions().size(), 1);
    }

    @Test
    public void test6() {
        Model s = new Model();
        BoolVar b = s.boolVar(false);
        IntVar x = s.intVar("x", 0, 3, true);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(s.getSolver().findAllSolutions().size(), 4);
    }

    @Test
    public void test7() {
        Model s = new Model();
        BoolVar b = s.boolVar(false);
        IntVar x = s.intVar("x", 0, 2, true);
        int c = 3;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(s.getSolver().findAllSolutions().size(), 3);
    }
}
