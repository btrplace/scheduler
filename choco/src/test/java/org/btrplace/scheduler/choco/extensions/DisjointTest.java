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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class DisjointTest {

    @Test
    public void test() {
        IntVar[] g1 = new IntVar[3];
        IntVar[] g2 = new IntVar[3];
        Solver s = new Solver();
        for (int i = 0; i < g1.length; i++) {
            g1[i] = VF.enumerated("G1-" + i, 0, (i + 1), s);
            g2[i] = VF.enumerated("G2-" + i, 0, (i + 1), s);
        }
        s.post(new Disjoint(g1, g2, 4));
        s.post(IntConstraintFactory.arithm(g2[g2.length - 1], "<=", g1[g1.length - 1]));
        s.findAllSolutions();
    }

    @Test
    public void disjointMultipleTest() {
        IntVar[][] groups = new IntVar[3][3];
        Solver s = new Solver();
        for (int g = 0; g < groups.length; g++) {
            for (int i = 0; i < groups[g].length; i++) {
                groups[g][i] = VF.enumerated("G" + g + "-" + i, 0, 2, s);
            }
        }
        s.post(new DisjointMultiple(groups, 3));
        for (int g = 1; g < groups.length; g++) {
            s.post(IntConstraintFactory.arithm(groups[g - 1][2], "<=", groups[g][2]));
        }
        //SMF.log(s, true, true);
        //SMF.logContradiction(s);
        s.findAllSolutions();

    }


}
