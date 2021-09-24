/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * A map to associate an integer to an element.
 * The map does not compact the key space so it is very efficient in terms of memory when there is no or a very few
 * holes. For the best performances, it is also wise to set the capacity as early as possible to bypass the incremental
 * expansion.
 */
public class IntMap {

    /**
     * Default size for the map.
     */
    public static final int DEFAULT_SIZE = 10;

    /**
     * Value stating that the key is missing.
     */
    private final int noValue;

    /**
     * Value backend.
     */
    private int[] values;

    /**
     * Biggest key in the map.
     */
    private int lastKey;

    /**
     * New map.
     *
     * @param noValue the value to use to report a missing key.
     */
    public IntMap(int noValue) {
        this(noValue, DEFAULT_SIZE);
    }

    /**
     * New map.
     *
     * @param noValue the value to use to report a missing key.
     * @param size    the backend size.
     */
    public IntMap(int noValue, int size) {
        this.noValue = noValue;
        this.values = new int[size];
        Arrays.fill(values, noValue);
    }

    /**
     * Copy constructor.
     *
     * @param backend the backend.
     * @param noValue the value to use to report a missing key.
     */
    private IntMap(final int[] backend, final int noValue, int last) {
        this.noValue = noValue;
        this.values = backend;
        this.lastKey = last;
    }

    /**
     * Expand the map to a new size.
     *
     * @param newSize the new size for the backend.
     */
    public void expand(final int newSize) {
        if (newSize > values.length) {
            int[] bigger = Arrays.copyOf(values, newSize);
            Arrays.fill(bigger, values.length, newSize, noValue);
            values = bigger;
        }
    }

    /**
     * Get the value for the key.
     * If the key is not in, {@link #noEntryValue()} is returned.
     *
     * @param key the key.
     * @return the value associated to the key. {@link #noEntryValue()} otherwise.
     */
    public int get(final int key) {
        if (key < 0 || key >= values.length) {
            return noValue;
        }
        return values[key];
    }

    /**
     * Unsafe version of {@link #get(int)}.
     *
     * @param key the key.
     * @return the value.
     */
    public int quickGet(final int key) {
        return values[key];
    }

    /**
     * Check if a key is in the map.
     *
     * @param key the key.
     * @return {@code true} if the key is in the map. This means that the value differs from {@link #noEntryValue()}.
     */
    public boolean has(final int key) {
        return get(key) != noValue;
    }

    /**
     * Get the value indicating the key is not in the map.
     *
     * @return the value.
     */
    public int noEntryValue() {
        return noValue;
    }

    /**
     * Put an entry in the map. The map is expanded if needed.
     *
     * @param key   the entry key.
     * @param value the value.
     */
    public void put(final int key, int value) {
        if (key >= values.length) {
            int curCap = values.length;
            // 50% grow at minimum, up to key.
            expand(Math.max(key + 1, curCap + curCap / 2));
        }
        values[key] = value;
        lastKey = Math.max(key, lastKey);
    }

    /**
     * Returns a copy of the map.
     *
     * @return a clean copy.
     */
    public IntMap copy() {
        int[] newBackend = Arrays.copyOf(values, values.length);
        return new IntMap(newBackend, noValue, lastKey);
    }

    /**
     * Clear the given key. The value will be set to {@link #noEntryValue()}.
     *
     * @param key the key.
     */
    public void clear(final int key) {
        if (key >= 0 && key < values.length) {
            values[key] = noValue;
        }
    }

    /**
     * Clear the map.
     */
    public void clear() {
        Arrays.fill(values, noValue);
        lastKey = 0;
    }

    /**
     * Iterate over the map entries.
     * This should not be used for any performance critical computation as the performance will depend on the key
     * density.
     *
     * @param e the iterator to use.
     */
    public void forEach(final Entry e) {
        for (int i = 0; i <= lastKey; i++) {
            final int v = values[i];
            if (v == noValue) {
                continue;
            }
            if (!e.entry(i, v)) {
                break;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IntMap intMap = (IntMap) o;
        return noValue == intMap.noValue && Arrays.equals(values, intMap.values);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(noValue);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }

    /**
     * Interface used to iterate over the entries.
     */
    @FunctionalInterface
    public interface Entry {

        /**
         * Gives an entry.
         *
         * @param k the key
         * @param v the value
         * @return {@code true} to continue the iteration.
         */
        boolean entry(final int k, final int v);
    }
}
