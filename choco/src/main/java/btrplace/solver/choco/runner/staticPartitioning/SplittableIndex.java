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

import btrplace.model.Element;
import btrplace.model.Node;
import btrplace.model.VM;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

/**
 * A array of elements that that will be grouped by their index key.
 * Element sharing a key will be accessible through {@link IndexEntry}
 * that might be created from {@link #forEachIndexEntry(IndexEntryProcedure)}.
 * <p/>
 * This approach is useful when a set of elements must be splitted into
 * multiple subsets as the elements are available into an {@link IndexEntry}
 * without any data duplication.
 *
 * @author Fabien Hermenier
 */
public class SplittableIndex<E extends Element> implements Comparator<E> {

    private TIntIntHashMap index;

    private E[] values;

    /**
     * Make a new splittable index.
     *
     * @param c     the index elements
     * @param index the index key associated to each element. Format {@link btrplace.model.Element#id()} -> key
     */
    public SplittableIndex(E[] c, TIntIntHashMap index) {
        values = c;
        this.index = index;
        Arrays.sort(values, this);
    }

    /**
     * Make a new splittable index from a collection of VM.
     * We consider the collection does not have duplicated elements.
     *
     * @param c     the collection to wrap
     * @param index the index associated to each VM
     * @return the resulting splittable index
     */
    public static SplittableIndex<VM> newVMIndex(Collection<VM> c, TIntIntHashMap index) {
        return new SplittableIndex<>(c.toArray(new VM[c.size()]), index);
    }

    /**
     * Make a new splittable index from a collection of nodes.
     * We consider the collection does not have duplicated elements.
     *
     * @param c     the collection to wrap
     * @param index the index associated to each node
     * @return the resulting splittable index
     */
    public static SplittableIndex<Node> newNodeIndex(Collection<Node> c, TIntIntHashMap index) {
        return new SplittableIndex<>(c.toArray(new Node[c.size()]), index);
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder("");
        forEachIndexEntry(new IndexEntryProcedure<E>() {
            @Override
            public void extract(SplittableIndex<E> index, int idx, int from, int to) {
                b.append("SplittableIndex ").append(idx).append(':');
                for (int i = from; i < to; i++) {
                    b.append(' ').append(values[i]);
                }
                b.append('\n');
            }
        });
        return b.toString();
    }

    /**
     * Execute a procedure on the values that have the same index key.
     *
     * @param p the procedure to execute
     */
    public void forEachIndexEntry(IndexEntryProcedure<E> p) {
        int curIdx = index.get(values[0].id());
        int from, to;
        for (from = 0, to = 0; to < values.length; to++) {
            int cIdx = index.get(values[to].id());
            if (curIdx != cIdx) {
                p.extract(this, curIdx, from, to);
                from = to;
                curIdx = cIdx;
            }
        }
        p.extract(this, curIdx, from, to);
    }

    @Override
    public int compare(E o1, E o2) {
        return index.get(o1.id()) - index.get(o2.id());
    }

    /**
     * Get the index associated to each element.
     *
     * @return a map of {@link btrplace.model.Element#id()} -> index value
     */
    public TIntIntHashMap getRespectiveIndex() {
        return index;
    }

    /**
     * Get the backend array of elements.
     *
     * @return a non-empty array
     */
    public E[] getValues() {
        return values;
    }
}


