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
import org.btrplace.model.IterateProcedure;
import org.btrplace.model.SplittableElementSet;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Seq;

import java.util.List;

/**
 * Splitter for {@link org.btrplace.model.constraint.Seq} constraints.
 * <p>
 * The splitting process is supported iff all the VMs belong to the same partitions.
 * If not, a {@link UnsupportedOperationException} is thrown.
 * <p>
 * This operation is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class SeqSplitter implements ConstraintSplitter<Seq> {

    @Override
    public Class<Seq> getKey() {
        return Seq.class;
    }

    @Override
    public boolean split(Seq cstr, Instance origin, final List<Instance> partitions, TIntIntHashMap vmsPosition, TIntIntHashMap nodePosition) {
        final List<VM> seq = cstr.getInvolvedVMs();
        return SplittableElementSet.newVMIndex(seq, vmsPosition).
                forEachPartition(new IterateProcedure<VM>() {

                    private boolean first = true;

                    @Override
                    public boolean extract(SplittableElementSet<VM> index, int idx, int from, int to) {
                        int size = to - from;
                        if (!first) {
                            throw new UnsupportedOperationException("Splitting a Seq over multiple partitions is not supported");
                        }
                        if (size == seq.size()) {
                            partitions.get(idx).getSatConstraints().add(new Seq(seq));
                            first = false;
                        }
                        return true;
                    }
                });
    }
}
