/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions;


import org.btrplace.scheduler.choco.extensions.pack.VectorPacking;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Unit tests for {@link org.btrplace.scheduler.choco.extensions.pack.VectorPacking}.
 *
 * @author Sophie Demassey
 */
public class VectorPackingTest {

    @Test
    public void test2DWithUnOrderedItems() {
        int nItems = 25;
        int[] height = new int[nItems];
        Random rnd = new Random(120);
        for (int i = 0; i < nItems; i++) {
            height[i] = rnd.nextInt(4);
        }

        Context ctx = new Context();
        ctx.modelPack2D(5, 20, height);
        ctx.testPack(true, "failed with heights " + Arrays.toString(height));
    }

    @Test()
    public void test2DLoadSup() {
        Context ctx = new Context();
        ctx.modelPack2D(3, 5, 3, 2);
        ctx.testPack(24);
    }

    @Test()
    public void test2DGuillaume() {
        Context ctx = new Context();
        ctx.modelPack2D(2, 100, 3, 30);
        IntVar margeLoad = ctx.s.intVar("margeLoad", 0, 50, true);
        ctx.s.post(ctx.s.element(margeLoad, ctx.loads[0], ctx.bins[0], 0));
        ctx.testPack(2);
    }

    @Test()
    public void test2DHeap() {
        Context ctx = new Context();
        ctx.modelPack2D(new int[]{2, 5, 3}, new int[]{5, 3, 1});
        ctx.testPack(1); // b[0]=1, b[1]=2, b[2]=0
    }

    @Test
    public void test2DBig() {
        Context ctx = new Context();
        int nItems = 10;
        int[] height = new int[nItems];
        for (int i = 0; i < nItems / 2; i++) {
            height[i] = 2;
        }
        for (int i = nItems / 2; i < nItems; i++) {
            height[i] = 1;
        }
        ctx.modelPack2D(5, 3, height);
        //s.set(IntStrategyFactory.inputOrder_InDomainMin(bins));
        ctx.testPack(true, "failed with " + Arrays.toString(height));
    }

    @Test
    public void testBig() {
        Context ctx = new Context();
        int nBins = 100;
        int nItems = nBins * 10;
        int[] capa = new int[]{16, 32};
        int[] height = new int[]{1, 1};
        ctx.modelPack(nBins, capa, nItems, height);
        ctx.s.getSolver().setSearch(Search.intVarSearch(new InputOrder<>(ctx.s), new IntDomainMin(), ctx.bins));
        ctx.testPack(true, "failed with " + Arrays.toString(height));
    }

    private static class Context {
        Model s;
        IntVar[][] loads;
        int[][] sizes;
        IntVar[] bins;

        public void modelPack2D(int nBins, int capa, int nItems, int height) {
            int[] heights = new int[nItems];
            Arrays.fill(heights, height);
            modelPack2D(nBins, capa, heights);
        }

        public void modelPack2D(int nBins, int capa, int[] height) {
            int[] capas = new int[nBins];
            Arrays.fill(capas, capa);
            modelPack2D(capas, height);
        }

        public void modelPack2D(int[] capa, int[] height) {
            modelPack(new int[][]{capa}, new int[][]{height});
        }

        public void modelPack(int nBins, int[] capa, int nItems, int[] height) {
            int nRes = capa.length;
            assert nRes == height.length;
            int[][] heights = new int[nRes][nItems];
            int[][] capas = new int[nRes][nBins];
            for (int d = 0; d < nRes; d++) {
                Arrays.fill(heights[d], height[d]);
                Arrays.fill(capas[d], capa[d]);
            }
            modelPack(capas, heights);
        }


        public void modelPack(int[][] capa, int[][] height) {
            int nRes = capa.length;
            assert nRes == height.length;
            int nBins = capa[0].length;
            int nItems = height[0].length;
            s = new Model();
            loads = new IntVar[nRes][nBins];
            bins = new IntVar[nItems];
            String[] name = new String[nRes];
            for (int d = 0; d < nRes; d++) {
                name[d] = "d" + d;
                for (int i = 0; i < nBins; i++) {
                    loads[d][i] = s.intVar("l" + d + "." + i, 0, capa[d][i], true);
                }
            }
            sizes = height;
            bins = s.intVarArray("b", nItems, 0, nBins, false);
            Constraint cPack = new VectorPacking(name, loads, sizes, bins);
            s.post(cPack);
        }

        public void testPack(boolean isFeasible, String errMsg) {
            s.getSolver().findSolution();
            Assert.assertEquals(s.getSolver().isFeasible(), ESat.eval(isFeasible), errMsg);
        }

        public void testPack(int nbExpectedSols) {
            List<Solution> sols = s.getSolver().findAllSolutions();
            int nbComputedSols = sols.size();
            Assert.assertEquals(s.getSolver().isFeasible(), ESat.eval(nbExpectedSols != 0), "SAT");
            if (nbExpectedSols > 0) {
                Assert.assertEquals(nbComputedSols, nbExpectedSols, "#SOL");
            }
        }
    }

}
