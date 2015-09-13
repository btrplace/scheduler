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


import org.btrplace.scheduler.choco.extensions.pack.VectorPacking;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.ESat;

import java.util.Arrays;
import java.util.Random;

/**
 * Unit tests for {@link org.btrplace.scheduler.choco.extensions.pack.VectorPacking}.
 *
 * @author Sophie Demassey
 */
public class VectorPackingTest {

    Solver s;
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
        s = new Solver();
        loads = new IntVar[nRes][nBins];
        bins = new IntVar[nItems];
        String[] name = new String[nRes];
        for (int d = 0; d < nRes; d++) {
            name[d] = "d" + d;
            for (int i = 0; i < nBins; i++) {
                loads[d][i] = VF.bounded("l" + d + "." + i, 0, capa[d][i], s);
            }
        }
        sizes = height;
        bins = VF.enumeratedArray("b", nItems, 0, nBins, s);
        Constraint cPack = new VectorPacking(name, loads, sizes, bins, true, true);
        s.post(cPack);
    }


    public void testPack(boolean isFeasible, String errMsg) {
        s.findSolution();
        Assert.assertEquals(s.isFeasible(), ESat.eval(isFeasible), errMsg);
    }

    public void testPack(int nbExpectedSols) {
        long nbComputedSols = s.findAllSolutions();

        Assert.assertEquals(s.isFeasible(), ESat.eval(nbExpectedSols != 0), "SAT");
        if (nbExpectedSols > 0) {
            Assert.assertEquals(nbComputedSols, nbExpectedSols, "#SOL");
        }

    }

    @Test
    public void test2DWithUnOrderedItems() {
        int nItems = 25;
        int[] height = new int[nItems];
        Random rnd = new Random(120);
        for (int i = 0; i < nItems; i++) {
            height[i] = rnd.nextInt(4);
        }
        System.out.println("Test with " + Arrays.toString(height));
        modelPack2D(5, 20, height);
        testPack(true, "failed with heights " + Arrays.toString(height));
    }

    @Test(sequential = true)
    public void test2DLoadSup() {
        modelPack2D(3, 5, 3, 2);
        testPack(24);
    }

    @Test(sequential = true)
    public void test2DGuillaume() {
        modelPack2D(2, 100, 3, 30);
        IntVar margeLoad = VF.bounded("margeLoad", 0, 50, s);
        s.post(IntConstraintFactory.element(margeLoad, loads[0], bins[0], 0));
        testPack(2);
    }

    @Test(sequential = true)
    public void test2DHeap() {
        modelPack2D(new int[]{2, 5, 3}, new int[]{5, 3, 1});
        testPack(1); // b[0]=1, b[1]=2, b[2]=0
    }

    @Test
    public void test2DBig() {
        int nItems = 10;
        int[] height = new int[nItems];
        for (int i = 0; i < nItems / 2; i++) {
            height[i] = 2;
        }
        for (int i = nItems / 2; i < nItems; i++) {
            height[i] = 1;
        }
        modelPack2D(5, 3, height);
        //s.set(IntStrategyFactory.inputOrder_InDomainMin(bins));
        testPack(true, "failed with " + Arrays.toString(height));
    }

    @Test
    public void testBig() {
        int nBins = 100;
        int nItems = nBins * 10;
        int[] capa = new int[]{16, 32};
        int[] height = new int[]{1, 1};
        modelPack(nBins, capa, nItems, height);
        s.set(ISF.custom(ISF.lexico_var_selector(), ISF.min_value_selector(), bins));
        testPack(true, "failed with " + Arrays.toString(height));
    }


}
