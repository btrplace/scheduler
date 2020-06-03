/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class DisjointTest {

    @Test
    public void test() {
        IntVar[] g1 = new IntVar[3];
        IntVar[] g2 = new IntVar[3];
        Model s = new Model();
        for (int i = 0; i < g1.length; i++) {
            g1[i] = s.intVar("G1-" + i, 0, (i + 1), false);
            g2[i] = s.intVar("G2-" + i, 0, (i + 1), false);
        }
        s.post(new Disjoint(g1, g2, 4));
        s.post(s.arithm(g2[g2.length - 1], "<=", g1[g1.length - 1]));
        s.getSolver().findAllSolutions();
    }

    @Test
    public void disjointMultipleTest() {
        IntVar[][] groups = new IntVar[3][3];
        Model s = new Model();
        for (int g = 0; g < groups.length; g++) {
            for (int i = 0; i < groups[g].length; i++) {
                groups[g][i] = s.intVar("G" + g + "-" + i, 0, 2, false);
            }
        }
        s.post(new DisjointMultiple(groups, 3));
        for (int g = 1; g < groups.length; g++) {
            s.post(s.arithm(groups[g - 1][2], "<=", groups[g][2]));
        }
        //SMF.log(s, true, true);
        //SMF.logContradiction(s);
        s.getSolver().findAllSolutions();

    }


}
