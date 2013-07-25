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

package btrplace.solver.choco.runner.staticPartitioning;

import btrplace.model.*;
import btrplace.model.constraint.MinMTTR;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithParams;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link FixedSizePartitioning}.
 *
 * @author Fabien Hermenier
 */
public class FixedSizePartitioningTest {

    @Test
    public void basicTesting() {
        FixedSizePartitioning f = new FixedSizePartitioning(1000);
        f.setWorkersCount(5);
        Assert.assertEquals(f.getWorkersCount(), 5);
        Assert.assertEquals(f.getSize(), 1000);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setInstanceSolver(f);
        Assert.assertEquals(cra.getInstanceSolver(), f);
    }

    @Test
    public void testSplit() throws SolverException {
        FixedSizePartitioning f = new FixedSizePartitioning(5);
        Model mo = new DefaultModel();
        for (int i = 0; i < 13; i++) {
            Node n = mo.newNode();
            mo.getMapping().addOnlineNode(n);
            for (int j = 0; j < 3; j++) {
                VM v = mo.newVM();
                mo.getMapping().addRunningVM(v, n);
            }
        }
        List<Instance> partitions = f.split(new DefaultChocoReconfigurationAlgorithParams(), new Instance(mo, Collections.<SatConstraint>emptyList(), new MinMTTR()));
        Assert.assertEquals(partitions.size(), 3);
        for (int i = 0; i < partitions.size(); i++) {
            Instance inst = partitions.get(i);
            if (i == 2) {
                Assert.assertEquals(inst.getModel().getMapping().getAllNodes().size(), 3);
                Assert.assertEquals(inst.getModel().getMapping().getAllVMs().size(), 9);
            } else {
                Assert.assertEquals(inst.getModel().getMapping().getAllNodes().size(), 5);
                Assert.assertEquals(inst.getModel().getMapping().getAllVMs().size(), 15);
            }
        }

        //Check the VMs are the nodes IDs are all registered to be substitution proof
        for (int i = 0; i < 39; i++) {
            if (i < 13) {
                Assert.assertTrue(partitions.get(0).getModel().contains(new Node(i)));
                Assert.assertTrue(partitions.get(1).getModel().contains(new Node(i)));
            }
            Assert.assertTrue(partitions.get(0).getModel().contains(new VM(i)));
            Assert.assertTrue(partitions.get(1).getModel().contains(new VM(i)));
        }
    }

    @Test
    public void testSplitVMsToLaunch() throws SolverException {
        FixedSizePartitioning f = new FixedSizePartitioning(5);
        f.setWorkersCount(2);
        Model mo = new DefaultModel();
        for (int i = 0; i < 20; i++) {
            Node n = mo.newNode();
            mo.getMapping().addOnlineNode(n);
            VM v = mo.newVM();
            mo.getMapping().addRunningVM(v, n); //1 VM per node is already running
        }
        //30 VMs to launch
        for (int i = 0; i < 30; i++) {
            VM v = mo.newVM();
            mo.getMapping().addReadyVM(v);
        }
        Instance origin = new Instance(mo, new MinMTTR());
        origin.getConstraints().add(new Running(mo.getMapping().getAllVMs()));
        List<Instance> subs = f.split(new DefaultChocoReconfigurationAlgorithParams(), origin);
        //Check disjoint set of ready VMs
        Set<VM> allReady = new HashSet<>();
        for (Instance i : subs) {
            allReady.addAll(i.getModel().getMapping().getReadyVMs());
        }
        Assert.assertEquals(allReady.size(), 30);

        //Quick solve
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setInstanceSolver(f);
        ReconfigurationPlan plan = cra.solve(origin.getModel(), origin.getConstraints(), origin.getOptimizationConstraint());
        Assert.assertEquals(plan.getSize(), 30); //all the VMs to launch have been booted
        System.out.println(cra.getStatistics());
        System.out.flush();

    }

    @Test
    public void testParallelSolvingWoConstraints() throws SolverException {
        FixedSizePartitioning f = new FixedSizePartitioning(50);
        f.setWorkersCount(2);
        Model mo = new DefaultModel();
        for (int i = 0; i < 150; i++) {
            Node n = mo.newNode();
            mo.getMapping().addOnlineNode(n);
            for (int j = 0; j < 3; j++) {
                VM v = mo.newVM();
                mo.getMapping().addRunningVM(v, n);
            }
        }
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setInstanceSolver(f);
        ReconfigurationPlan plan = cra.solve(mo, Collections.<SatConstraint>emptyList());
        Assert.assertEquals(plan.getSize(), 0);
        System.out.println(cra.getStatistics());
        System.out.flush();
    }
}