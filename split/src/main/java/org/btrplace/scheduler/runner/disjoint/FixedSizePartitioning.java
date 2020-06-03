/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint;

import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
        super(Collections.singleton(new HashSet<>()));
        this.partSize = s;
        random = false;
    }

    /**
     * Get the maximum partition size in terms of number of nodes.
     *
     * @return a value &gt; 0
     */
    public int getSize() {
        return partSize;
    }

    /**
     * Set the maximum partition size in terms of number of nodes.
     *
     * @param s a value &gt; 0
     */
    public void setSize(int s) {
        this.partSize = s;
    }

    @Override
    public List<Instance> split(Parameters ps, Instance i) throws SchedulerException {
        Mapping map = i.getModel().getMapping();

        setPartitions(random ? randomPartitions(ps.getRandomSeed(), map) : linearPartitions(map));
        return super.split(ps, i);
    }


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

    private List<Collection<Node>> randomPartitions(long seed, Mapping map) {
        Random rnd = new Random(seed);
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
