/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
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
 */public class FastIFFEqTest {

    @Test
    public void test1() {
        Model csp = new Model();
        //SMF.log(s, true, true);
        BoolVar b = csp.boolVar("b");
        IntVar x = csp.intVar("x", 0, 3, true);
        int c = 2;
        csp.post(new FastIFFEq(b, x, c));
        Assert.assertEquals(4, csp.getSolver().findAllSolutions().size());
    }

    @Test
    public void test2() {
        Model csp = new Model();
        BoolVar b = csp.boolVar("b");
        IntVar x = csp.intVar("x", 0, 3, false);
        int c = 2;
        csp.post(new FastIFFEq(b, x, c));
        Assert.assertEquals(4, csp.getSolver().findAllSolutions().size());
    }

    @Test
    public void test3() {
        Model csp = new Model();
        BoolVar b = csp.boolVar("b");
        IntVar x = csp.intVar("x", 0, 2, true);
        int c = 3;
        csp.post(new FastIFFEq(b, x, c));
        Assert.assertEquals(3, csp.getSolver().findAllSolutions().size());
    }

    @Test
    public void test4() {
        Model csp = new Model();
        BoolVar b = csp.boolVar(true);
        IntVar x = csp.intVar("x", 0, 2);
        int c = 3;
        csp.post(new FastIFFEq(b, x, c));
        Assert.assertEquals(0, csp.getSolver().findAllSolutions().size());
    }

    @Test
    public void test5() {
        Model csp = new Model();
        BoolVar b = csp.boolVar(true);
        IntVar x = csp.intVar("x", 0, 3);
        int c = 2;
        csp.post(new FastIFFEq(b, x, c));
        Assert.assertEquals(1, csp.getSolver().findAllSolutions().size());
    }

    @Test
    public void test6() {
        Model csp = new Model();
        BoolVar b = csp.boolVar(false);
        IntVar x = csp.intVar("x", 0, 3, true);
        int c = 2;
        csp.post(new FastIFFEq(b, x, c));
        Assert.assertEquals(3, csp.getSolver().findAllSolutions().size());
    }

    @Test
    public void test7() {
        Model csp = new Model();
        BoolVar b = csp.boolVar(false);
        IntVar x = csp.intVar("x", 0, 2, true);
        int c = 3;
        csp.post(new FastIFFEq(b, x, c));
        Assert.assertEquals(3, csp.getSolver().findAllSolutions().size());
    }
}
