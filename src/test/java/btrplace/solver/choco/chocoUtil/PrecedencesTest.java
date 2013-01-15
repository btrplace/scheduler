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
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
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
     * ends variables are already instantiated.
     * Just a simple test.
     */
    @Test
    public void dummyTest() {
        CPSolver s = new CPSolver();
        ChocoLogging.setVerbosity(Verbosity.FINEST);

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
}
