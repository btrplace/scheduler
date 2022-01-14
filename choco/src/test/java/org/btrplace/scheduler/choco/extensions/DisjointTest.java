/*
 * Copyright  2022 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.search.limits.SolutionCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class DisjointTest {

    private static final Random rnd = new Random();

    @Test
    public void testNoSolutions() {
        Model mo = new Model();
        IntVar[][] vars = new IntVar[][]{
                new IntVar[]{mo.intVar(0, 1), mo.intVar(0, 1)},
                new IntVar[]{mo.intVar(2, 3), mo.intVar(2, 3)},
                new IntVar[]{mo.intVar(1, 2), mo.intVar(1, 4)},
        };
        new Disjoint(vars, 5).post();
        mo.allDifferent(vars[0]).post();
        mo.allDifferent(vars[1]).post();
        // vars[2][0] cannot take any value as 1 will be used in vars[0] and 2 in vars[1].
        Assert.assertFalse(mo.getSolver().solve());
    }

    /**
     * Test the constraint with groups having disjoint domains.
     * Accordingly, the constraint is entailed from the begging and every possible assignment will lead to a solution.
     */
    @Test
    public void testNonOverlappingGroups() {
        Model mo = new Model();
        IntVar[][] vars = new IntVar[][]{
                new IntVar[]{mo.intVar(0, 2), mo.intVar(2, 4)},
                new IntVar[]{mo.intVar(5, 6), mo.intVar(5, 6), mo.intVar(7, 8)},
                new IntVar[]{mo.intVar(10, 12)}
        };
        Disjoint dm = new Disjoint(vars, 13);
        dm.post();
        while (mo.getSolver().solve()) ;
        // As the groups have non-overlapping domains, the number of solutions is the product of the variable domains.
        Assert.assertEquals(3 * 3 * 2 * 2 * 2 * 3, mo.getSolver().getSolutionCount());
    }

    /**
     * Test the constraint with groups having disjoint domains.
     * Accordingly, the constraint is entailed from the begging and every possible assignment will lead to a solution.
     */
    @Test
    public void test1() {
        Model mo = new Model();
        IntVar[][] vars = new IntVar[][]{
                new IntVar[]{mo.intVar(0, 3), mo.intVar(2)}, // Here we have (0,2) (1,2) (2,2) (3,2)
                // Below, 2 is never doable because of vars[0][1] thus the value will be 3. As a consequence, (3,2) is
                // not possible before.
                new IntVar[]{mo.intVar(2, 3)},
                new IntVar[]{mo.intVar(new int[]{2, 4, 5, 6})}, // 2 is never doable thus up to 3 values.
        };
        Disjoint dm = new Disjoint(vars, 13);
        dm.post();
        while (mo.getSolver().solve()) ;
        // As the groups have non-overlapping domains, the number of solutions is the product of the variable domains.
        Assert.assertEquals(9, mo.getSolver().getSolutionCount());
    }

    @Test
    public void testIsSatisfied() {
        Model mo = new Model();
        IntVar[][] vars = new IntVar[][]{
                new IntVar[]{mo.intVar(0), mo.intVar(1)},
                new IntVar[]{mo.intVar(2), mo.intVar(2), mo.intVar(2), mo.intVar(7)},
                new IntVar[]{mo.intVar(4), mo.intVar(1)},
        };

        // False as 1 is in group 2 and 0.
        Assert.assertEquals(ESat.FALSE, new Disjoint(vars, 8).isSatisfied());

        // True as the error has been fixed.
        vars[2][1] = mo.intVar(4);
        Assert.assertEquals(ESat.TRUE, new Disjoint(vars, 8).isSatisfied());

        // False: 4 is in group 1 and 2.
        vars[1][3] = mo.intVar(4);
        Assert.assertEquals(ESat.FALSE, new Disjoint(vars, 8).isSatisfied());

        // True: Last group is only 6. No more violation with group 1.
        vars[2] = new IntVar[]{mo.intVar(6)};
        Assert.assertEquals(ESat.TRUE, new Disjoint(vars, 8).isSatisfied());

        // False: 6 in group 1 and 2.
        vars[1][3] = mo.intVar(6);
        Assert.assertEquals(ESat.FALSE, new Disjoint(vars, 8).isSatisfied());
    }

    /**
     * Test against a formulation based on != constraints.
     */
    @Test
    public void testCorrectness() {
        for (int i = 0; i < 20; i++) {
            long seed = System.currentTimeMillis();
            int[][][] data = data();
            Set<Map<String, Integer>> s1 = solutions(model(data, false), seed);
            Set<Map<String, Integer>> s2 = solutions(model(data, true), seed);
            Assert.assertEquals(s1, s2);
        }
    }

    private Set<Map<String, Integer>> solutions(final Model m, long seed) {
        m.getSolver().setSearch(Search.randomSearch(m.retrieveIntVars(true), seed));
        List<Solution> sols = m.getSolver().findAllSolutions(new SolutionCounter(m, 100));
        Set<Map<String, Integer>> all = new HashSet<>();
        for (final Solution sol : sols) {
            Map<String, Integer> map = new HashMap<>();
            for (final IntVar var : m.retrieveIntVars(true)) {
                map.put(var.getName(), sol.getIntVal(var));
            }
            all.add(map);
        }
        return all;
    }

    private int[][][] data() {
        int nbGroups = rnd.nextInt(3) + 2;
        int[][][] data = new int[nbGroups][][];
        for (int g = 0; g < nbGroups; g++) {
            int nbVars = rnd.nextInt(5) + 3;
            data[g] = new int[nbVars][2];
            for (int v = 0; v < nbVars; v++) {
                int lb = rnd.nextInt(5);
                int ub = rnd.nextInt(4) + lb;
                data[g][v][0] = lb;
                data[g][v][1] = ub;
            }
        }
        return data;
    }

    private Model model(int[][][] data, boolean reformulation) {
        Model mo = new Model();
        IntVar[][] vars = new IntVar[data.length][];
        int max = 0;
        for (int g = 0; g < data.length; g++) {
            vars[g] = new IntVar[data[g].length];
            for (int v = 0; v < data[g].length; v++) {
                IntVar var = mo.intVar("(" + g + "," + v + ")", data[g][v][0], data[g][v][1]);
                vars[g][v] = var;
                max = Math.max(max, data[g][v][1]);
            }
        }
        if (reformulation) {
            for (int g1 = 0; g1 < vars.length; g1++) {
                for (int g2 = g1 + 1; g2 < vars.length; g2++) {
                    for (int v1 = 0; v1 < vars[g1].length; v1++) {
                        for (int v2 = 0; v2 < vars[g2].length; v2++) {
                            mo.arithm(vars[g1][v1], "!=", vars[g2][v2]).post();
                        }
                    }
                }
            }
        } else {
            Disjoint dd = new Disjoint(vars, max + 1);
            dd.post();
        }
        return mo;
    }
}
