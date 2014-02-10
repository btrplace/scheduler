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
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.variables.IntVar;
import solver.variables.VF;

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
        Constraint cPack = new LightBinPacking(new String[]{"foo"}, s.getEnvironment(), new IntVar[][]{loads}, new int[][]{sizes}, bins);
        s.post(cPack);
        //s.getConfiguration().putFalse(Configuration.STOP_AT_FIRST_SOLUTION);
    }

    public void testPack(int nbSol) {
        /*s.getConfiguration().putFalse(Configuration.STOP_AT_FIRST_SOLUTION);
        s.generateSearchStrategy();
        s.launch();*/
        /*Assert.assertEquals((boolean) s.isFeasible(), nbSol != 0, "SAT");
        if (nbSol > 0) {
            Assert.assertEquals(s.getNbSolutions(), nbSol, "#SOL");
        } */
        ///s.clear();
    }

    @Test
    public void testWithUnOrderedItems() {
        int nBins = 5;
        int nItems = 25;
        s = new Solver();
        loads = new IntVar[nBins];
        sizes = new int[nItems];
        bins = new IntVar[nItems];
        for (int i = 0; i < nBins; i++) {
            loads[i] = VF.bounded("l" + i, 0, 20, s);
        }
        Random rnd = new Random();
        for (int i = 0; i < nItems; i++) {
            sizes[i] = rnd.nextInt(4);
            bins[i] = VF.enumerated("b" + i, 0, nBins, s);
        }
        Constraint cPack = new LightBinPacking(new String[]{"foo"}, s.getEnvironment(), new IntVar[][]{loads}, new int[][]{sizes}, bins);
        s.post(cPack);

        //s.getConfiguration().putTrue(Configuration.STOP_AT_FIRST_SOLUTION);
        ////s.generateSearchStrategy();
        //s.launch();
        //int nbSol = s.getNbSolutions();
        //Assert.assertEquals((boolean) s.isFeasible(), nbSol != 0, "SAT");
        //s.clear();
    }

    @Test(sequential = true)
    public void testLoadSup() {
        modelPack(3, 5, 3, 2);
        //s.addGoal(BranchingFactory.minDomMinVal(s, bins));
        testPack(24);
    }

    @Test(sequential = true)
    public void testGuillaume() {
        modelPack(2, 100, 3, 30);
        IntVar margeLoad = VF.bounded("margeLoad", 0, 50, s);
        s.post(nth(bins[0], loads, margeLoad));
        testPack(2);
    }

    /**
     * var = array[index]
     */
    public Constraint nth(IntVar index, IntVar[] array, IntVar var) {
        return IntConstraintFactory.element(var, array, index, 0);
    }

}
