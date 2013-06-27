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
import btrplace.model.constraint.SatConstraint;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithmParams;
import btrplace.solver.choco.runner.staticPartitioning.splitter.ConstraintSplitterMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A partitioning algorithm that create partitions having a fixed number of nodes.
 * VMs to run are distributed among the partitions.
 * <p/>
 * The partitioning does not consider constraints other that state-oriented constraints.
 *
 * @author Fabien Hermenier
 */
public class FixedPartitionSize extends StaticPartitioning {

    private int partSize;

    private ConstraintSplitterMapper cstrMapper;

    /**
     * Make a new partitioning algorithm.
     *
     * @param partSize the maximum partition size
     */
    public FixedPartitionSize(int partSize) {
        this.partSize = partSize;
        cstrMapper = new ConstraintSplitterMapper();
    }

    /**
     * Get the maximum partition size in terms of number of nodes.
     *
     * @return a value > 0
     */
    public int getSize() {
        return partSize;
    }

    @Override
    public List<Instance> split(ChocoReconfigurationAlgorithmParams ps, Instance i) throws SolverException {
        Model mo = i.getModel();
        Mapping map = mo.getMapping();
        int nbPartitions = (int) Math.ceil(1.0 * map.getAllNodes().size() / partSize);
        List<Set<Node>> partOfNodes = new ArrayList<>();
        Set<Node> curPartition = new HashSet<>(partSize);
        partOfNodes.add(curPartition);
        for (Node node : map.getAllNodes()) {
            if (curPartition.size() == partSize) {
                curPartition = new HashSet<>(partSize);
                partOfNodes.add(curPartition);
            }
            curPartition.add(node);
        }

        List<Instance> parts = new ArrayList<>(nbPartitions);
        for (Set<Node> s : partOfNodes) {
            Model partModel = new DefaultModel();
            //Book every node and VMs id, we will re-use them in practice
            for (Node n : i.getModel().getNodes()) {
                partModel.newNode(n.id());
            }
            for (VM vm : i.getModel().getVMs()) {
                partModel.newVM(vm.id());
            }

            parts.add(new Instance(partModel, new HashSet<SatConstraint>(), i.getOptimizationConstraint()));
            for (Node n : s) {
                if (map.getOfflineNodes().contains(n)) {
                    partModel.getMapping().addOfflineNode(n);
                } else {
                    partModel.getMapping().addOnlineNode(n);
                    for (VM v : map.getRunningVMs(n)) {
                        partModel.getMapping().addRunningVM(v, n);
                    }
                    for (VM v : map.getSleepingVMs(n)) {
                        partModel.getMapping().addRunningVM(v, n);
                    }
                }
            }
        }
        for (SatConstraint cstr : i.getConstraints()) {
            cstrMapper.split(cstr, parts);
        }
        //TODO: deal with ready VMs to run and state-oriented constraints
        return parts;
    }
}
