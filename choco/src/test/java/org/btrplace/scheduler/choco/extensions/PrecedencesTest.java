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
        Model s = new Model();

        IntVar[] ends = new IntVar[5];
        int[] others = new int[5];
        others[0] = 0;
        ends[0] = s.intVar(1);
        others[1] = 0;
        ends[1] = s.intVar(3);
        others[2] = 1;
        ends[2] = s.intVar(2);
        others[3] = 1;
        ends[3] = s.intVar(4);
        others[4] = 2;
        ends[4] = s.intVar(5);


        IntVar host = s.intVar("host", 0, 2, false);
        IntVar start = s.intVar("start", 0, 5, true);
        /*
           If host == 0, consume = 3,4,5
           If host == 1, consume = 4,5
           If host == 2, consume = 5
           => 6 solutions
         */
        Precedences p = new Precedences(host, start, others, ends);
        s.post(p);
        Assert.assertEquals(s.getSolver().findAllSolutions().size(), 6);
    }

    /**
     * Ends variables vary between 1 and 2 + index of the host
     * Just a simple test.
     */
    @Test
    public void simpleTest() {
        Model s = new Model();

        IntVar[] ends = new IntVar[3];
        int[] others = new int[3];
        ends[0] = s.intVar("ends[0]", 1, 2, true);
        ends[1] = s.intVar("ends[1]", 1, 3, true);
        ends[2] = s.intVar("ends[2]", 1, 4, true);

        /*
         on host 0, 2 * 2 * 2 -> 8
         on host 1, 2 * 2 -> 4

         on host 2, 4 * 4 * 2 -> 32
         16 * 9 * 16 + 27 * 4 * 16 + 32 * 4 * 9
         */

        IntVar host = s.intVar(0, 0);
        IntVar start = s.intVar(0, 5, true);
        Precedences p = new Precedences(host, start, others, ends);
        s.post(p);
        Assert.assertEquals(s.getSolver().findAllSolutions().size(), 75); //TODO: A way to check if it is correct ? :D
    }
}
