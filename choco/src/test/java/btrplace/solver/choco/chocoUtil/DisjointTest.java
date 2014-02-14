/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.search.loop.monitors.SMF;
import solver.variables.IntVar;
import solver.variables.VF;

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
        s.post(new Disjoint(s, g1, g2, 4));
        s.post(IntConstraintFactory.arithm(g2[g2.length - 1], "<=", g1[g1.length - 1]));
        SMF.log(s, true, true);
        s.findAllSolutions();
    }
}
