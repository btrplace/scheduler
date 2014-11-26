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

package org.btrplace.scheduler.choco.runner.disjoint.splitter;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.Instance;
import org.btrplace.model.constraint.Constraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A customizable bridge to indicate which {@link ConstraintSplitter} to use
 * for a given constraint.
 *
 * @author Fabien Hermenier
 */
public class ConstraintSplitterMapper {

    private Map<Class<? extends Constraint>, ConstraintSplitter> builders;

    /**
     * Make a new bridge.
     */
    public ConstraintSplitterMapper() {
        builders = new HashMap<>();
    }

    /**
     * Make a new bridge and register
     * every splitters supported by default.
     *
     * @return the fulfilled bridge.
     */
    public static ConstraintSplitterMapper newBundle() {
        ConstraintSplitterMapper mapper = new ConstraintSplitterMapper();

        mapper.register(new AmongSplitter());
        mapper.register(new BanSplitter());
        mapper.register(new FenceSplitter());
        mapper.register(new GatherSplitter());
        mapper.register(new KilledSplitter());
        mapper.register(new LonelySplitter());
        mapper.register(new OfflineSplitter());
        mapper.register(new OnlineSplitter());
        mapper.register(new OverbookSplitter());
        mapper.register(new PreserveSplitter());
        mapper.register(new QuarantineSplitter());
        mapper.register(new ReadySplitter());
        mapper.register(new RootSplitter());
        mapper.register(new RunningSplitter());
        mapper.register(new SeqSplitter());
        mapper.register(new SleepingSplitter());
        mapper.register(new SplitSplitter());
        mapper.register(new SpreadSplitter());

        return mapper;
    }

    /**
     * Register a splitter.
     *
     * @param ccb the splitter to register
     * @return {@code true} if no splitter previously registered for the given constraint was deleted
     */
    public boolean register(ConstraintSplitter<? extends Constraint> ccb) {
        return builders.put(ccb.getKey(), ccb) == null;
    }

    /**
     * Un-register the splitter associated to a given {@link Constraint} if exists.
     *
     * @param c the class of the {@link Constraint} to un-register
     * @return {@code true} if a builder was registered
     */
    public boolean unRegister(Class<? extends Constraint> c) {
        return builders.remove(c) != null;
    }

    /**
     * Check if a {@link ConstraintSplitter} is registered for a given {@link Constraint}.
     *
     * @param c the constraint to check
     * @return {@code true} iff a builder is registered
     */
    public boolean isRegistered(Class<? extends Constraint> c) {
        return builders.containsKey(c);
    }

    /**
     * Get the splitter associated to a {@link Constraint}.
     *
     * @param c the constraint
     * @return the associated builder if exists. {@code null} otherwise
     */
    public ConstraintSplitter getSplitter(Class<? extends Constraint> c) {
        return builders.get(c);
    }

    /**
     * Split a given {@link Constraint} using the associated splitter, if exists.
     *
     * @param c            the constraint to map
     * @param i            the original instance to split
     * @param partitions   the partitions splitting the original instance
     * @param vmPartition  the partition associated to each VM
     * @param nodePosition the partition associated to each node
     * @return {@code false} iff this leads to a problem without solutions.
     */
    public boolean split(Constraint c, Instance i, List<Instance> partitions, TIntIntHashMap vmPartition, TIntIntHashMap nodePosition) {
        ConstraintSplitter splitter = builders.get(c.getClass());
        return splitter != null && splitter.split(c, i, partitions, vmPartition, nodePosition);
    }
}
