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
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithParams;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

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
        /*Assert.assertEquals(partitions.get(0).getModel().getVMs().size(), 39);
        Assert.assertEquals(partitions.get(0).getModel().getVMs(), partitions.get(1).getModel().getVMs());
        Assert.assertEquals(partitions.get(0).getModel().getVMs(), partitions.get(2).getModel().getVMs());

        Assert.assertEquals(partitions.get(0).getModel().getNodes().size(), 13);
        Assert.assertEquals(partitions.get(0).getModel().getNodes(), partitions.get(1).getModel().getNodes());
        Assert.assertEquals(partitions.get(0).getModel().getNodes(), partitions.get(2).getModel().getNodes());*/
    }

    @Test
    public void testParallelSolving() throws SolverException {
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
