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

package btrplace.model;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.*;

/**
 * A collection of supposed unique {@link Element} that can be split
 * into multiple sub-sets.
 * <p/>
 * The partitioning is provided by an index that indicate, at instantiation,
 * the right partition for each element.
 * <p/>
 * The backend is a simple array of elements. Elements belonging to the same
 * partition are contiguous for efficiency.
 *
 * @author Fabien Hermenier
 */
public class SplittableElementSet<E extends Element> implements Comparator<E> {

    private TIntIntHashMap index;

    private E[] values;

    /**
     * Make a new splittable set.
     *
     * @param c     the elements, no duplicates are supposed
     * @param index the partition associated to each element. Format {@link btrplace.model.Element#id()} -> key
     */
    public SplittableElementSet(E[] c, TIntIntHashMap index) {
        values = c;
        this.index = index;
        Arrays.sort(values, this);
    }

    /**
     * Make a new splittable set from a collection of VM.
     * We consider the collection does not have duplicated elements.
     *
     * @param c     the collection to wrap
     * @param index the partition for each VM
     * @return the resulting set
     */
    public static SplittableElementSet<VM> newVMIndex(Collection<VM> c, TIntIntHashMap index) {
        return new SplittableElementSet<>(c.toArray(new VM[c.size()]), index);
    }

    /**
     * Make a new splittable set from a collection of nodes.
     * We consider the collection does not have duplicated elements.
     *
     * @param c     the collection to wrap
     * @param index the partition for each node
     * @return the resulting set
     */
    public static SplittableElementSet<Node> newNodeIndex(Collection<Node> c, TIntIntHashMap index) {
        return new SplittableElementSet<>(c.toArray(new Node[c.size()]), index);
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder("{");
        forEachPartition(new IterateProcedure<E>() {
            @Override
            public boolean extract(SplittableElementSet<E> index, int idx, int from, int to) {
                b.append('{');
                b.append(values[from]);
                for (int i = from + 1; i < to; i++) {
                    b.append(", ").append(values[i]);
                }
                b.append('}');
                return true;
            }
        });
        return b.append('}').toString();
    }

    /**
     * Execute a procedure on each partition.
     * The partition is indicated by its bounds on the backend array.
     *
     * @param p the procedure to execute
     */
    public boolean forEachPartition(IterateProcedure<E> p) {
        int curIdx = index.get(values[0].id());
        int from, to;
        for (from = 0, to = 0; to < values.length; to++) {
            int cIdx = index.get(values[to].id());
            if (curIdx != cIdx) {
                if (!p.extract(this, curIdx, from, to)) {
                    return false;
                }
                from = to;
                curIdx = cIdx;
            }
        }
        return p.extract(this, curIdx, from, to);
    }

    /**
     * Get a subset for the given partition.
     *
     * @param k the partition key
     * @return the resulting subset
     */
    public ElementSubSet<E> getSubset(int k) {
        int from = -1;
        //TODO: very bad. Bounds such be memorized
        for (int x = 0; x < values.length; x++) {
            int cIdx = index.get(values[x].id());
            if (cIdx == k && from == -1) {
                from = x;
            }
            if (from >= 0 && cIdx > k) {
                return new ElementSubSet<>(this, k, from, x);
            }
        }
        if (from >= 0) {
            return new ElementSubSet<>(this, k, from, values.length);
        }
        return null;
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

    /**
     * Get all the partitions as subsets.
     *
     * @return a collection of {@link ElementSubSet}.
     */
    public List<ElementSubSet<E>> getPartitions() {
        final List<ElementSubSet<E>> partitions = new ArrayList<>();
        forEachPartition(new IterateProcedure<E>() {
            @Override
            public boolean extract(SplittableElementSet<E> index, int key, int from, int to) {
                partitions.add(new ElementSubSet<>(SplittableElementSet.this, key, from, to));
                return true;
            }
        });
        return partitions;
    }

    /**
     * Get the size of the set.
     *
     * @return a positive integer
     */
    public int size() {
        return values.length;
    }
}


