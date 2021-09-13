/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions;


import org.btrplace.model.*;
import org.btrplace.model.constraint.MinMigrations;
import org.btrplace.model.constraint.Running;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.extensions.pack.VectorPacking;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;
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

import java.util.ArrayList;
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


    /**
     * 1GB free on every node and only 2GB VMs. We ask for booting a 2GB VM. No
     * solution detected immediately if the hosting capacity is capped to the
     * max number of VMs per node (100) as their sum will not exceed the
     * number of running VMs.
     */
    @Test
    public void testSlotFilteringWithUniformVMs() {
        int capa = 201;
        DefaultModel mo = new DefaultModel();
        Mapping map = mo.getMapping();
        ShareableResource mem = new ShareableResource("mem");
        ShareableResource cpu = new ShareableResource("cpu");
        ShareableResource ctrl = new ShareableResource("ctrl");
        mo.attach(mem);
        mo.attach(cpu);
        mo.attach(ctrl);
        Instance ii = new Instance(mo, new ArrayList<>(), new MinMigrations());
        for (int i = 0; i < 500; i++) {
            final Node no = mo.newNode();
            map.on(no);
            mem.setCapacity(no, capa);
            cpu.setCapacity(no, capa);
            ctrl.setCapacity(no, capa);
            // 1 left on every node.
            for (int j = 0; j < capa / 2; j++) {
                final VM vm = mo.newVM();
                map.run(no, vm);
                mem.setConsumption(vm, 2);
                cpu.setConsumption(vm, 2);
                ctrl.setConsumption(vm, 2);
            }
            final VM vm = mo.newVM();
            map.run(no, vm);
            mem.setConsumption(vm, 0);
            cpu.setConsumption(vm, 0);
            ctrl.setConsumption(vm, 0);
        }
        final VM p = mo.newVM();
        mem.setConsumption(p, 2);
        cpu.setConsumption(p, 2);
        ctrl.setConsumption(p, 2);
        map.addReadyVM(p);
        final ChocoScheduler sched = new DefaultChocoScheduler();
        ii.getSatConstraints().add(new Running(p));
        sched.doRepair(true);
        ReconfigurationPlan plan = sched.solve(ii);
        Assert.assertNull(plan);

        // The problem is stated during the initial propagation.
        SolvingStatistics stats = sched.getStatistics();
        Assert.assertEquals(0, stats.getMetrics().nodes());
        // With 0 size VMs, same conclusion.
        for (Node no : map.getOnlineNodes()) {
            final VM vm = mo.newVM();
            map.run(no, vm);
            mem.setConsumption(vm, 0);
            ctrl.setConsumption(vm, 0);
            cpu.setConsumption(vm, 0);
        }
        plan = sched.solve(ii);
        Assert.assertNull(plan);

        // The problem is stated during the initial propagation.
        stats = sched.getStatistics();
        Assert.assertEquals(0, stats.getMetrics().nodes());
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
            Constraint cPack = new VectorPacking(name, loads, sizes, bins, false);
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
