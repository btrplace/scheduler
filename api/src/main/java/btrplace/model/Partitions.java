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

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Partitions<T> {

    private TObjectIntHashMap<T> positions;

    private Partition<T> [] parts;

    private int [] bounds;

    private T [] data;

    public Partitions(Set<T> vs, int nbParts) {
        positions = new TObjectIntHashMap<>(vs.size());
        parts = new Partition[nbParts];
        data = (T[]) vs.toArray(new Object[vs.size()]);
        for (int i = 0; i < nbParts; i++) {
            parts[i] = new Partition<>(this, i);
        }
        for (int i = 0; i < data.length; i++) {
            positions.put(data[i], i);
        }
        bounds = new int[nbParts];
        Arrays.fill(bounds, data.length);
    }

    public boolean putIn(T v, int p) {
        if (!positions.containsKey(v)) {
            return false;
        }
        p++;  //+1 for the init hidden partition
        int idx = positions.put(v, p);
        int curPart = getPart(idx);
        System.err.println("Value " + v + " from @" + idx + " to part " + p + " (@" + bounds[p] + "): currently: " + toString());
        for (int i = curPart; i < p; i++) {
            swapWithLast(idx, bounds[curPart] - 1);
            shift(i);
        }
        return true;
    }

    private void shift(int p) {
        bounds[p]--;
        //System.err.println("After shifting part " + p + ": " + toString());

    }
    private void swapWithLast(int x, int y) {
        T v = data[x];
        data[x] = data[y];
        data[y] = v;
        //System.err.println("After swaping: " + toString());
    }
    public int getPart(int idx) {
        if (bounds[0] > idx) {
            return 0;
        }
        for (int i = 0; i < bounds.length - 1; i++) {
            if (bounds[i] <= idx && bounds[i + 1] >= idx) {
                return i;
            }
        }
        return bounds.length - 1; //last partition
    }

    public int getSize(int p) {
        System.err.println("Size for partition " + p + " : " + toString());
        return bounds[p + 1] - bounds[p];
    }

    public int getPosition(T v) {
        return positions.get(v);
    }

    public Partition<T>[] getPartitions() {
        return parts;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(positions.size());
        buf.append('[');
        int curPart = 0;
        for(int i = 0; i < data.length; i++) {
            while (curPart < bounds.length && i == bounds[curPart]) {
                buf.append('|');
                curPart++;
            }
            buf.append(data[i]).append(' ');
        }
        while (curPart < bounds.length) {
            buf.append('|');
            curPart++;
        }
        buf.append(']');
        buf.append(" bounds= ").append(Arrays.toString(bounds));
        return buf.toString();
    }
}
