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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

/**
 * Unit tests for {@link Precedences}.
 *
 * @author Fabien Hermenier
 */
public class PrecedencesTest {

    /**
     * Ends variables are already instantiated.
     * Just a simple test.
     */
    @Test
    public void dummyTest() {
        Solver s = new Solver();

        IntVar[] ends = new IntVar[5];
        int[] others = new int[5];
        others[0] = 0;
        ends[0] = VF.fixed(1, s);
        others[1] = 0;
        ends[1] = VF.fixed(3, s);
        others[2] = 1;
        ends[2] = VF.fixed(2, s);
        others[3] = 1;
        ends[3] = VF.fixed(4, s);
        others[4] = 2;
        ends[4] = VF.fixed(5, s);


        IntVar host = VF.enumerated("host", 0, 2, s);
        IntVar start = VF.bounded("start", 0, 5, s);
        /*
           If host == 0, consume = 3,4,5
           If host == 1, consume = 4,5
           If host == 2, consume = 5
           => 6 solutions
         */
        Precedences p = new Precedences(host, start, others, ends);
        s.post(p);
        Assert.assertEquals(6, s.findAllSolutions());
    }

    /**
     * Ends variables vary between 1 and 2 + index of the host
     * Just a simple test.
     */
    @Test
    public void simpleTest() {
        Solver s = new Solver();

        IntVar[] ends = new IntVar[3];
        int[] others = new int[3];
        others[0] = 0;
        ends[0] = VF.bounded("ends[0]", 1, 2, s);
        others[1] = 0;
        ends[1] = VF.bounded("ends[1]", 1, 3, s);
        others[2] = 0;
        ends[2] = VF.bounded("ends[2]", 1, 4, s);

        /*
         on host 0, 2 * 2 * 2 -> 8
         on host 1, 2 * 2 -> 4

         on host 2, 4 * 4 * 2 -> 32
         16 * 9 * 16 + 27 * 4 * 16 + 32 * 4 * 9
         */

        IntVar host = VF.enumerated("host", 0, 0, s);
        IntVar start = VF.bounded("start", 0, 5, s);
        Precedences p = new Precedences(host, start, others, ends);
        s.post(p);
        Assert.assertEquals(s.findAllSolutions(), 75); //TODO: A way to check if it is correct ? :D
    }
}
