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

import btrplace.model.Instance;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithmParams;

import java.util.*;

/**
 * A partitioning algorithm that create partitions having a fixed number of nodes.
 * VMs to run are distributed among the partitions.
 * <p/>
 * The partitioning does not consider constraints other that state-oriented constraints.
 *
 * @author Fabien Hermenier
 */
public class FixedSizePartitioning extends FixedNodeSetsPartitioning {

    private int partSize;

    private boolean random;

    /**
     * Make a new partitioning algorithm.
     *
     * @param partSize the maximum partition size
     */
    public FixedSizePartitioning(int partSize) {
        super(Collections.<Collection<Node>>singleton(new HashSet<Node>()));
        this.partSize = partSize;
        random = false;
    }

    /**
     * Get the maximum partition size in terms of number of nodes.
     *
     * @return a value > 0
     */
    public int getSize() {
        return partSize;
    }

    public void setSize(int s) {
        this.partSize = s;
    }

    @Override
    public List<Instance> split(ChocoReconfigurationAlgorithmParams ps, Instance i) throws SolverException {
        Model mo = i.getModel();
        Mapping map = mo.getMapping();
        List<Collection<Node>> partOfNodes = new ArrayList<>();
        Set<Node> curPartition = new HashSet<>(partSize);
        partOfNodes.add(curPartition);
        for (Node node : map.getAllNodes()) {
            if (curPartition.size() == partSize) {
                curPartition = new HashSet<>(partSize);
                partOfNodes.add(curPartition);
            }
            curPartition.add(node);
        }

        setPartitions(partOfNodes);
        return super.split(ps, i);
    }

    public void randomPickUp(boolean b) {
        this.random = b;
    }

    public boolean randomPickUp() {
        return this.random;
    }
}
