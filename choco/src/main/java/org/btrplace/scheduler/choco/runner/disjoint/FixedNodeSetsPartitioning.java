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

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import org.btrplace.model.*;
import org.btrplace.model.constraint.Running;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.runner.disjoint.splitter.ConstraintSplitterMapper;

import java.util.*;

/**
 * A partitioning algorithm to split an instance
 * into multiple disjoint sub-instances from a
 * specific node partitioning.
 * Running and sleeping VMs are spread on the sub-instances
 * depending on their current location while ready VMs are spread
 * evenly.
 * <p>
 * The {@link SatConstraint}s are split when necessary using
 * splitters available through the {@link ConstraintSplitterMapper}.
 * The {@link org.btrplace.model.constraint.OptConstraint} is re-used
 * for each sub-instance.
 *
 * @author Fabien Hermenier
 */
public class FixedNodeSetsPartitioning extends StaticPartitioning {

    private Collection<Collection<Node>> partitions;

    private ConstraintSplitterMapper cstrMapper;

    /**
     * Make a new partitioning algorithm.
     * By default, the partition algorithm use the {@link ConstraintSplitterMapper}
     * returned by {@link org.btrplace.scheduler.choco.runner.disjoint.splitter.ConstraintSplitterMapper#newBundle()}.
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
     * @param m the mapper
     */
    public void setSplitterMapper(ConstraintSplitterMapper m) {
        this.cstrMapper = m;
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
     * sets were not disjoint
     */
    public boolean setPartitions(Collection<Collection<Node>> parts) {
        if (!isDisjoint(parts)) {
            return false;
        }
        partitions = parts;
        return true;
    }

    @Override
    public List<Instance> split(Parameters ps, Instance i) throws SchedulerException {
        Model mo = i.getModel();

        SynchronizedElementBuilder eb = new SynchronizedElementBuilder(mo);

        List<Instance> parts = new ArrayList<>(partitions.size());

        //nb of VMs
        int nbVMs = i.getModel().getMapping().getNbVMs();
        int nbNodes = i.getModel().getMapping().getNbNodes();
        TIntIntHashMap vmPosition = new TIntIntHashMap(nbVMs);
        TIntIntHashMap nodePosition = new TIntIntHashMap(nbNodes);

        int partNumber = 0;

        Set<VM> toLaunch = getVMsToLaunch(i);

        for (Collection<Node> s : partitions) {
            SubModel partModel = new SubModel(mo, eb, s, new HashSet<VM>(toLaunch.size() / partitions.size()));

            parts.add(new Instance(partModel, new THashSet<SatConstraint>(), i.getOptConstraint()));

            //VM Index
            partModel.getMapping().fillVMIndex(vmPosition, partNumber);
            //Node index
            for (Node n : s) {
                nodePosition.put(n.id(), partNumber);
            }
            partNumber++;
        }

        //Round-robin placement for the VMs to launch
        int p = 0;
        for (VM v : toLaunch) {
            if (!parts.get(p).getModel().getMapping().addReadyVM(v)) {
                throw new SchedulerException(parts.get(p).getModel(), "Unable to dispatch the VM to launch '" + v + "'");
            }
            vmPosition.put(v.id(), p);
            p = ((p + 1) % parts.size());
        }

        //Split the constraints
        for (SatConstraint cstr : i.getSatConstraints()) {
            if (!cstrMapper.split(cstr, i, parts, vmPosition, nodePosition)) {
                throw new SchedulerException(i.getModel(), "Unable to split " + cstr);
            }
        }

        return parts;
    }

    private Set<VM> getVMsToLaunch(Instance i) {
        Mapping m = i.getModel().getMapping();
        Set<VM> toLaunch = new THashSet<>();
        for (SatConstraint cstr : i.getSatConstraints()) {
            //Extract the VMs to launch
            if (cstr instanceof Running) {
                for (VM v : cstr.getInvolvedVMs()) {
                    if (m.isReady(v)) {
                        m.remove(v);
                        toLaunch.add(v);
                    }
                }
            }
        }
        return toLaunch;
    }

    private static boolean isDisjoint(Collection<Collection<Node>> p) {
        TIntHashSet all = new TIntHashSet();
        for (Collection<Node> s : p) {
            for (Node n : s) {
                if (!all.add(n.id())) {
                    return false;
                }
            }
        }
        return true;
    }
}
