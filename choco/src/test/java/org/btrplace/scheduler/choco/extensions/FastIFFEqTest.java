/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
