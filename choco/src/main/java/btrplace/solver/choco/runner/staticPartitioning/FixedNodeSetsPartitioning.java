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
import btrplace.model.view.ModelView;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithmParams;
import btrplace.solver.choco.runner.staticPartitioning.splitter.ConstraintSplitterMapper;
import gnu.trove.TIntIntHashMap;

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

        TIntIntHashMap vmPosition = new TIntIntHashMap();
        int partNumber = 0;
        for (Collection<Node> s : partitions) {
            SubModel partModel = new SubModel(mo, eb, s);

            Instance i2 = new Instance(partModel, new HashSet<SatConstraint>(), i.getOptimizationConstraint());
            parts.add(i2);

            for (VM v : partModel.getMapping().myVMs()) {
                vmPosition.put(v.id(), partNumber);
            }
            partNumber++;
        }
        for (SatConstraint cstr : i.getConstraints()) {
            cstrMapper.split(cstr, i, parts, vmPosition);
        }
        //TODO: deal with ready VMs to run
        return parts;
    }

    private void makeMapping(Mapping src, Mapping dst, Collection<Node> s) {
        for (Node n : s) {
            if (src.getOfflineNodes().contains(n)) {
                dst.addOfflineNode(n);
            } else {
                dst.addOnlineNode(n);
                for (VM v : src.getRunningVMs(n)) {
                    dst.addRunningVM(v, n);
                }
                for (VM v : src.getSleepingVMs(n)) {
                    dst.addSleepingVM(v, n);
                }
            }
        }
    }

    private Model makeSubModel(Model src, SynchronizedElementBuilder p, Collection<Node> s) {
        Model mo = new DefaultModel(p);
        //Copy the attributes and the views
        for (ModelView v : src.getViews()) {
            mo.attach(v);
        }
        makeMapping(src.getMapping(), mo.getMapping(), s);
        mo.setAttributes(src.getAttributes());
        return mo;
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
