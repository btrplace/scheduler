/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.chocoUtil;

import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.testng.Assert;
import org.testng.annotations.Test;

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
        CPSolver s = new CPSolver();
        //ChocoLogging.setVerbosity(Verbosity.FINEST);

        IntDomainVar[] ends = new IntDomainVar[5];
        int[] others = new int[5];
        others[0] = 0;
        ends[0] = s.makeConstantIntVar(1);
        others[1] = 0;
        ends[1] = s.makeConstantIntVar(3);
        others[2] = 1;
        ends[2] = s.makeConstantIntVar(2);
        others[3] = 1;
        ends[3] = s.makeConstantIntVar(4);
        others[4] = 2;
        ends[4] = s.makeConstantIntVar(5);


        IntDomainVar host = s.createEnumIntVar("host", 0, 2);
        IntDomainVar start = s.createBoundIntVar("start", 0, 5);
        /*
           If host == 0, start = 3,4,5
           If host == 1, start = 4,5
           If host == 2, start = 5
           => 6 solutions
         */
        Precedences p = new Precedences(s.getEnvironment(), host, start, others, ends);
        s.post(p);
        Boolean ret = s.solveAll();
        Assert.assertEquals(ret, Boolean.TRUE);
        Assert.assertEquals(s.getNbSolutions(), 6);
    }

    /**
     * Ends variables vary between 1 and 2 + index of the host
     * Just a simple test.
     */
    @Test
    public void simpleTest() {
        CPSolver s = new CPSolver();
        //ChocoLogging.setVerbosity(Verbosity.FINEST);

        IntDomainVar[] ends = new IntDomainVar[3];
        int[] others = new int[3];
        others[0] = 0;
        ends[0] = s.createBoundIntVar("ends[0]", 1, 2);
        others[1] = 0;
        ends[1] = s.createBoundIntVar("ends[1]", 1, 3);
        others[2] = 0;
        ends[2] = s.createBoundIntVar("ends[2]", 1, 4);

        /*
         on host 0, 2 * 2 * 2 -> 8
         on host 1, 2 * 2 -> 4

         on host 2, 4 * 4 * 2 -> 32
         16 * 9 * 16 + 27 * 4 * 16 + 32 * 4 * 9
         */

        IntDomainVar host = s.createEnumIntVar("host", 0, 0);
        IntDomainVar start = s.createBoundIntVar("start", 0, 5);
        Precedences p = new Precedences(s.getEnvironment(), host, start, others, ends);
        s.post(p);
        Boolean ret = s.solveAll();
        Assert.assertEquals(ret, Boolean.TRUE);
        Assert.assertEquals(s.getNbSolutions(), 75); //TODO: A way to check if it is correct ?
    }
}
