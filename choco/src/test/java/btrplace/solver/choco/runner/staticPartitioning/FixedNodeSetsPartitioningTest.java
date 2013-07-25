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
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithmParams;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link FixedNodeSetsPartitioning}.
 *
 * @author Fabien Hermenier
 */
public class FixedNodeSetsPartitioningTest {

    private List<Collection<Node>> splitIn(Set<Node> s, int nb) {
        List<Collection<Node>> partOfNodes = new ArrayList<>();
        Set<Node> curPartition = new HashSet<>(nb);
        partOfNodes.add(curPartition);

        for (Node node : s) {
            if (curPartition.size() == nb) {
                curPartition = new HashSet<>(nb);
                partOfNodes.add(curPartition);
            }
            curPartition.add(node);
        }
        return partOfNodes;
    }

    @Test
    public void testSplit() throws SolverException {

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

        List<Collection<Node>> parts = splitIn(mo.getMapping().getAllNodes(), 3);
        FixedNodeSetsPartitioning f = new FixedNodeSetsPartitioning(parts);
        f.setWorkersCount(3);

        origin.getConstraints().add(new Running(mo.getMapping().getAllVMs()));
        List<Instance> subs = f.split(new DefaultChocoReconfigurationAlgorithmParams(), origin);
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
}
