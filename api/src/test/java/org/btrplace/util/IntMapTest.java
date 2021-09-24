/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.util;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class IntMapTest {

    @Test
    public void testPutHasGet() {
        final IntMap m = new IntMap(-1);
        Assert.assertEquals(m.noEntryValue(), -1);
        Assert.assertFalse(m.has(0));
        Assert.assertEquals(m.get(0), -1);

        Assert.assertFalse(m.has(56));
        m.put(56, 12);
        Assert.assertEquals(m.get(56), 12);
        Assert.assertTrue(m.has(56));

        Assert.assertEquals(m.get(-1), -1);
        Assert.assertFalse(m.has(-1));

        m.put(4, 5);
        Assert.assertEquals(m.get(4), 5);

        m.put(56, 3);
        Assert.assertEquals(m.get(56), 3);
    }

    @Test
    public void testForEach() {
        final IntMap m = new IntMap(-1);
        for (int i = 0; i < 10; i += 2) {
            m.put(i, i * 2);
        }
        final AtomicInteger count = new AtomicInteger(0);
        m.forEach((k, v) -> {
            Assert.assertEquals(k % 2, 0);
            Assert.assertEquals(v, k * 2);
            count.incrementAndGet();
            return true;
        });
        Assert.assertEquals(count.get(), 5);
        count.set(0);
        m.forEach((k, v) -> {
            count.incrementAndGet();
            return false;
        });
        Assert.assertEquals(1, count.get());
    }

    @Test
    public void testClear() {
        final IntMap m = new IntMap(-1);
        m.put(7, 12);
        m.clear(7);
        m.clear(-1);
        Assert.assertEquals(m.quickGet(7), -1);

        for (int i = 5; i < 100; i++) {
            m.put(i, i * 2);
        }
        m.clear();
        m.forEach((k, v) -> {
            Assert.fail("No entry expected");
            return true;
        });
    }

    @Test
    public void testCopy() {
        final IntMap m = new IntMap(-1);
        for (int i = 0; i < 100; i += 2) {
            m.put(i, i * 2);
        }
        IntMap cp = m.copy();
        Assert.assertEquals(cp, m);
        Assert.assertEquals(cp.hashCode(), m.hashCode());

        // Remove a key that does not exist.
        cp.clear(5);
        Assert.assertEquals(m, cp);

        cp.clear(2);
        Assert.assertNotEquals(m, cp);
    }
}