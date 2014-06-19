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

package btrplace.solver.choco.extensions;


import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.search.loop.monitors.SMF;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VF;
import util.ESat;

import java.util.Arrays;
import java.util.Random;

/**
 * Unit tests for {@link LightBinPacking}.
 *
 * @author Sophie Demassey
 */
public class LightBinPackingTest {

    Solver s;
    IntVar[] loads;
    int[] sizes;
    IntVar[] bins;


    public void modelPack(int nBins, int capa, int nItems, int height) {
        int[] heights = new int[nItems];
        Arrays.fill(heights, height);
        modelPack(nBins, capa, heights);
    }

    public void modelPack(int nBins, int capa, int[] height) {
        int[] capas = new int[nBins];
        Arrays.fill(capas, capa);
        modelPack(capas, height);
    }

    public void modelPack(int[] capa, int[] height) {
        int nBins = capa.length;
        int nItems = height.length;
        s = new Solver();
        loads = new IntVar[nBins];
        sizes = new int[nItems];
        bins = new IntVar[nItems];
        for (int i = 0; i < nBins; i++) {
            loads[i] = VF.bounded("l" + i, 0, capa[i], s);
        }
        for (int i = 0; i < nItems; i++) {
            sizes[i] = height[i];
            bins[i] = VF.enumerated("b" + i, 0, nBins, s);
        }
        Constraint cPack = new LightBinPacking(new String[]{"foo"}, new IntVar[][]{loads}, new int[][]{sizes}, bins);
        s.post(cPack);
        //s.getConfiguration().putFalse(Configuration.STOP_AT_FIRST_SOLUTION);
    }

    public void testPack(boolean isFeasible) {
        //s.getConfiguration().putFalse(Configuration.STOP_AT_FIRST_SOLUTION);
        //s.generateSearchStrategy();
        //s.launch();
        SMF.log(s, true, true);
        SMF.logContradiction(s);
        s.findSolution();
        Assert.assertEquals(s.isFeasible(), ESat.eval(isFeasible), "SAT");
    }

    public void testPack(int nbExpectedSols) {
        SMF.log(s, true, true);
        SMF.logContradiction(s);
        long nbComputedSols = s.findAllSolutions();

        Assert.assertEquals(s.isFeasible(), ESat.eval(nbExpectedSols != 0), "SAT");
        if (nbExpectedSols > 0) {
            Assert.assertEquals(nbComputedSols, nbExpectedSols, "#SOL");
        }

    }

    @Test
    public void testWithUnOrderedItems() {
        int nItems = 25;
        sizes = new int[nItems];
        Random rnd = new Random();
        for (int i = 0; i < nItems; i++) {
            sizes[i] = rnd.nextInt(4);
        }
        modelPack(5, 20, sizes);
        //s.set(IntStrategyFactory.inputOrder_InDomainMin(bins));
        testPack(true);

    }

    @Test(sequential = true)
    public void testLoadSup() {
        modelPack(3, 5, 3, 2);
        testPack(24);
    }

    @Test(sequential = true)
    public void testGuillaume() {
        modelPack(2, 100, 3, 30);
        IntVar margeLoad = VF.bounded("margeLoad", 0, 50, s);
        s.post(IntConstraintFactory.element(margeLoad, loads, bins[0], 0));
        testPack(2);
    }

    @Test(sequential = true)
    public void testHeap() {
        modelPack(new int[]{2, 5, 3}, new int[]{5, 3, 1});
        testPack(1);
    }

    @Test
    public void testBig() {
        int nItems = 10;
        sizes = new int[nItems];
        for (int i = 0; i < nItems/2; i++) {
            sizes[i] = 2;
        }
        for (int i = nItems/2; i < nItems; i++) {
            sizes[i] = 1;
        }
        modelPack(5, 3, sizes);
        //s.set(IntStrategyFactory.inputOrder_InDomainMin(bins));
        testPack(true);

    }


}
