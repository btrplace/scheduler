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

package org.btrplace.model;

import java.util.*;

/**
 * A set of elements in a {@link SplittableElementSet}
 * that are in the same partition.
 * <p>
 * This set is considered as immutable.
 *
 * @author Fabien Hermenier
 */
public class ElementSubSet<E extends Element> implements Set<E> {

    private int from, to;

    private int curIdx;

    private SplittableElementSet<E> index;

    /**
     * Make a new subset.
     *
     * @param parent the splittable parent set
     * @param key    the current partition identifier
     * @param lb     the lower bound in the backend array where elements start to have the given key
     * @param ub     the upper bound in the backend array where elements ends to have the given key (exclusive)
     */
    public ElementSubSet(SplittableElementSet<E> parent, int key, int lb, int ub) {
        this.index = parent;
        this.curIdx = key;
        this.from = lb;
        this.to = ub;
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
    public Iterator<E> iterator() {
        return new IndexEntryIterator<>(index.getValues(), from, to);
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOfRange(index.getValues(), from, to);
    }

    @Override
    public Object[] toArray(Object[] a) {

        Object[] r = a.length >= to - from ? a : (E[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), to - from);
        Iterator<E> it = iterator();

        for (int i = 0; i < r.length; i++) {
            if (!it.hasNext()) {
                // fewer elements than expected, null-terminate
                r[i] = null;
                return r;
            }
            r[i] = it.next();
        }
        return r;
    }

    @Override
    public boolean add(E o) {
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
     * The iterator associated to an {@link ElementSubSet}.
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
         * @param v  the values to iterate on
         * @param lb the initial index.
         * @param ub the last index (exclusive)
         */
        public IndexEntryIterator(E[] v, int lb, int ub) {
            this.values = v;
            this.to = ub;
            this.cursor = lb;

        }

        @Override
        public boolean hasNext() {
            return cursor != to;
        }

        @Override
        public E next() {
            if (cursor == to) {
                throw new NoSuchElementException();
            }
            return values[cursor++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
