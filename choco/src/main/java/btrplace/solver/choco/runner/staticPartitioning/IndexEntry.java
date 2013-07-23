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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * A set of elements that have the same index key.
 * This set is considered as immutable.
 *
 * @author Fabien Hermenier
 */
public class IndexEntry<E> implements Set<E> {

    private int from, to;

    private int curIdx;

    private SplittableIndex index;

    /**
     * Make a new entry.
     *
     * @param index the index to rely on
     * @param key   the current index key
     * @param from  the lower bound in the backend array where elements start to have the given index key
     * @param to    the upper bound in the backend array where elements ends to have the given index key (exclusive)
     */
    public IndexEntry(SplittableIndex index, int key, int from, int to) {
        this.index = index;
        this.curIdx = key;
        this.from = from;
        this.to = to;
    }

    @Override
    public int size() {
        return to - from;
    }

    @Override
    public boolean isEmpty() {
        return to == from;
    }

    @Override
    public boolean contains(Object o) {
        try {
            Element x = (Element) o;
            return index.getRespectiveIndex().get(x.id()) == curIdx;
        } catch (ClassCastException ex) {
            return false;
        }
    }

    @Override
    public Iterator iterator() {
        return new IndexEntryIterator(index.getValues(), from, to);
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOfRange(index.getValues(), from, to);
    }

    @Override
    public Object[] toArray(Object[] a) {
        Arrays.copyOfRange(a, from, to);
        return a;
    }

    @Override
    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("{").append(index.getValues()[from]);
        for (int i = from + 1; i < to; i++) {
            b.append(", ").append(index.getValues()[i]);
        }
        return b.append('}').toString();
    }

    /**
     * The iterator associated to an {@link IndexEntry}.
     *
     * @author Fabien Hermenier
     */
    public static class IndexEntryIterator<E> implements Iterator<E> {

        private E[] values;

        private int to;

        private int cursor;

        /**
         * Make a new iterator.
         *
         * @param values the values to iterator over
         * @param from   the initial index.
         * @param to     the last index (exclusive)
         */
        public IndexEntryIterator(E[] values, int from, int to) {
            this.values = values;
            this.to = to;
            this.cursor = from;

        }

        @Override
        public boolean hasNext() {
            return cursor != to;
        }

        @Override
        public E next() {
            return values[cursor++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
