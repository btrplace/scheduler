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

package org.btrplace.scheduler.choco.runner.disjoint;

import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;

import java.util.*;

/**
 * An extension of {@link FixedNodeSetsPartitioning} where
 * the partitions of nodes are computed by provided their
 * size in terms of number of nodes.
 *
 * @author Fabien Hermenier
 */
public class FixedSizePartitioning extends FixedNodeSetsPartitioning {

    private int partSize;

    private boolean random;

    /**
     * Make a new partitioning algorithm.
     *
     * @param s the maximum partition size
     */
    public FixedSizePartitioning(int s) {
        super(Collections.<Collection<Node>>singleton(new HashSet<Node>()));
        this.partSize = s;
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

    /**
     * Set the maximum partition size in terms of number of nodes.
     *
     * @param s a value > 0
     */
    public void setSize(int s) {
        this.partSize = s;
    }

    @Override
    public List<Instance> split(Parameters ps, Instance i) throws SchedulerException {
        Mapping map = i.getModel().getMapping();

        setPartitions(random ? randomPartitions(map) : linearPartitions(map));
        return super.split(ps, i);
    }

    private static Random rnd = new Random();

    private List<Collection<Node>> linearPartitions(Mapping map) {
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
        return partOfNodes;
    }

    private List<Collection<Node>> randomPartitions(Mapping map) {
        List<Node> unselectedNodes = new ArrayList<>(map.getNbNodes());
        unselectedNodes.addAll(map.getOnlineNodes());
        unselectedNodes.addAll(map.getOfflineNodes());

        List<Collection<Node>> partOfNodes = new ArrayList<>();
        Set<Node> curPartition = new HashSet<>(partSize);
        partOfNodes.add(curPartition);
        while (!unselectedNodes.isEmpty()) {
            Node n = unselectedNodes.remove(rnd.nextInt(unselectedNodes.size()));
            if (curPartition.size() == partSize) {
                curPartition = new HashSet<>(partSize);
                partOfNodes.add(curPartition);
            }
            curPartition.add(n);
        }

        return partOfNodes;
    }

    /**
     * Ask to select the nodes to put into distinct partitions randomly or not.
     *
     * @param b {@code true} for a random pick up
     */
    public void randomPickUp(boolean b) {
        this.random = b;
    }

    /**
     * Indicate if the nodes are picked up randomly.
     *
     * @return {@code true} for a random pickup
     */
    public boolean randomPickUp() {
        return this.random;
    }
}
