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
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.SynchronizedElementBuilder;
import btrplace.model.constraint.SatConstraint;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithmParams;
import btrplace.solver.choco.runner.staticPartitioning.splitter.ConstraintSplitterMapper;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.*;

/**
 * A partitioning algorithm that part an instance
 * wrt. predefined partition of nodes.
 *
 * @author Fabien Hermenier
 */
public class FixedNodeSetsPartitioning extends StaticPartitioning {

    private Collection<Collection<Node>> partitions;

    private ConstraintSplitterMapper cstrMapper;

    /**
     * Make a new partitioning algorithm.
     * By default, the partition algorithm use the {@link ConstraintSplitterMapper}
     * returned by {@link btrplace.solver.choco.runner.staticPartitioning.splitter.ConstraintSplitterMapper#newBundle()}.
     *
     * @param parts the node partitions to rely on
     */
    public FixedNodeSetsPartitioning(Collection<Collection<Node>> parts) {

        if (!isDisjoint(parts)) {
            throw new IllegalArgumentException("The constraint expects disjoint sets of nodes");

        }
        partitions = parts;
        cstrMapper = ConstraintSplitterMapper.newBundle();
    }

    /**
     * Get the mapper that is used to split the constraints.
     *
     * @return a mapper
     */
    public ConstraintSplitterMapper getSplitterMapper() {
        return cstrMapper;
    }

    /**
     * Set the mapper to use to split constraints.
     *
     * @param cstrMapper the mapper
     */
    public void setSplitterMapper(ConstraintSplitterMapper cstrMapper) {
        this.cstrMapper = cstrMapper;
    }

    /**
     * Get the node partitions.
     *
     * @return multiple collections of nodes
     */
    public Collection<Collection<Node>> getPartitions() {
        return partitions;
    }

    /**
     * Set the node partitions
     *
     * @param parts disjoint set of nodes
     * @return {@code true} iff the partitions have been set. {@code false} if the
     *         sets were not disjoint
     */
    public boolean setPartitions(Collection<Collection<Node>> parts) {
        if (!isDisjoint(parts)) {
            return false;
        }
        partitions = parts;
        return true;
    }

    @Override
    public List<Instance> split(ChocoReconfigurationAlgorithmParams ps, Instance i) throws SolverException {
        Model mo = i.getModel();

        SynchronizedElementBuilder eb = new SynchronizedElementBuilder(mo);

        List<Instance> parts = new ArrayList<>(partitions.size());

        //nb of VMs
        int nbVMs = i.getModel().getMapping().getReadyVMs().size();
        int nbNodes = i.getModel().getMapping().getOnlineNodes().size() + i.getModel().getMapping().getOfflineNodes().size();
        for (Node n : i.getModel().getMapping().getOnlineNodes()) {
            nbVMs += i.getModel().getMapping().getRunningVMs(n).size();
            nbVMs += i.getModel().getMapping().getSleepingVMs(n).size();
        }
        TIntIntHashMap vmPosition = new TIntIntHashMap(nbVMs);
        TIntIntHashMap nodePosition = new TIntIntHashMap(nbNodes);

        int partNumber = 0;
        for (Collection<Node> s : partitions) {
            SubModel partModel = new SubModel(mo, eb, s);

            parts.add(new Instance(partModel, new THashSet<SatConstraint>(), i.getOptimizationConstraint()));

            //VM Index
            partModel.getMapping().fillVMIndex(vmPosition, partNumber);
            //Node index
            for (Node n : s) {
                nodePosition.put(n.id(), partNumber);
            }
            partNumber++;
        }
        for (SatConstraint cstr : i.getConstraints()) {
            cstrMapper.split(cstr, i, parts, vmPosition, nodePosition);
        }
        //TODO: deal with ready VMs to run
        return parts;
    }

    private static boolean isDisjoint(Collection<Collection<Node>> p) {
        int cnt = 0;
        Set<Node> all = new HashSet<>();
        for (Collection<Node> s : p) {
            cnt += s.size();
            all.addAll(s);
            if (cnt != all.size()) {
                return false;
            }
        }
        return true;
    }
}
