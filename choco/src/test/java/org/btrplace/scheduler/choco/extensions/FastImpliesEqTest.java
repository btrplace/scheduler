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
 */public class FastImpliesEqTest {

    @Test
    public void test1() {
        Model s = new Model();
        //SMF.log(s, true, true);
        BoolVar b = s.boolVar("b");
        IntVar x = s.intVar("x", 0, 3, true);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(5, s.getSolver().findAllSolutions().size());
    }

    @Test
    public void test2() {
        Model s = new Model();
        BoolVar b = s.boolVar("b");
        IntVar x = s.intVar("x", 0, 3, false);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(5, s.getSolver().findAllSolutions().size());
    }

    @Test
    public void test3() {
        Model s = new Model();
        BoolVar b = s.boolVar("b");
        IntVar x = s.intVar("x", 0, 2, true);
        int c = 3;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(3, s.getSolver().findAllSolutions().size());
    }

    @Test
    public void test4() {
        Model s = new Model();
        BoolVar b = s.boolVar(true);
        IntVar x = s.intVar("x", 0, 2, true);
        int c = 3;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(0, s.getSolver().findAllSolutions().size());
    }

    @Test
    public void test5() {
        Model s = new Model();
        BoolVar b = s.boolVar(true);
        IntVar x = s.intVar("x", 0, 3, true);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(1, s.getSolver().findAllSolutions().size());
    }

    @Test
    public void test6() {
        Model s = new Model();
        BoolVar b = s.boolVar(false);
        IntVar x = s.intVar("x", 0, 3, true);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(4, s.getSolver().findAllSolutions().size());
    }

    @Test
    public void test7() {
        Model s = new Model();
        BoolVar b = s.boolVar(false);
        IntVar x = s.intVar("x", 0, 2, true);
        int c = 3;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(3, s.getSolver().findAllSolutions().size());
    }
}
