/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

/*
 * Created on 18/09/14.
 *
 * @author Sophie Demassey
 */public class FastImpliesEqTest {

    @Test
    public void test1() {
        Solver s = new Solver();
        //SMF.log(s, true, true);
        BoolVar b = VF.bool("b", s);
        IntVar x = VF.bounded("x", 0, 3, s);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(5, s.findAllSolutions());
    }

    @Test
    public void test2() {
        Solver s = new Solver();
        BoolVar b = VF.bool("b", s);
        IntVar x = VF.enumerated("x", 0, 3, s);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(5, s.findAllSolutions());
    }

    @Test
    public void test3() {
        Solver s = new Solver();
        BoolVar b = VF.bool("b", s);
        IntVar x = VF.bounded("x", 0, 2, s);
        int c = 3;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(3, s.findAllSolutions());
    }

    @Test
    public void test4() {
        Solver s = new Solver();
        BoolVar b = VF.one(s);
        IntVar x = VF.bounded("x", 0, 2, s);
        int c = 3;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(0, s.findAllSolutions());
    }

    @Test
    public void test5() {
        Solver s = new Solver();
        BoolVar b = VF.one(s);
        IntVar x = VF.bounded("x", 0, 3, s);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(1, s.findAllSolutions());
    }

    @Test
    public void test6() {
        Solver s = new Solver();
        BoolVar b = VF.zero(s);
        IntVar x = VF.bounded("x", 0, 3, s);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(4, s.findAllSolutions());
    }

    @Test
    public void test7() {
        Solver s = new Solver();
        BoolVar b = VF.zero(s);
        IntVar x = VF.bounded("x", 0, 2, s);
        int c = 3;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(3, s.findAllSolutions());
    }
}
