/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class IntMapTest {

    @Test
    public void testPutHasGet() {
        final IntMap m = new IntMap(-1);
        Assert.assertEquals(m.noEntryValue(), -1);
        Assert.assertFalse(m.has(0));
        Assert.assertEquals(m.get(0), -1);

        Assert.assertEquals(m.put(-1, 7), -1);
        Assert.assertFalse(m.has(56));
        Assert.assertEquals(m.put(56, 12), -1);
        Assert.assertEquals(m.get(56), 12);
        Assert.assertTrue(m.has(56));

        Assert.assertEquals(m.get(-1), -1);
        Assert.assertFalse(m.has(-1));

        Assert.assertEquals(m.put(4, 5), -1);
        Assert.assertEquals(m.get(4), 5);

        Assert.assertEquals(m.put(56, 3), 12);
        Assert.assertEquals(m.get(56), 3);
    }

    @Test
    public void testForEach() {
        IntMap m = new IntMap(0, 0);
        m.forEach((k, v) -> {
            Assert.fail();
            return true;
        });

        m = new IntMap(-1);
        m.forEach((k, v) -> {
            Assert.fail();
            return true;
        });
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
        Assert.assertEquals(m.clear(7), 12);
        Assert.assertEquals(m.clear(-1), -1);
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
        Assert.assertTrue(m.equals(m));
        Assert.assertNotEquals(new Object(), m);
        IntMap cp = m.copy();

        Assert.assertEquals(m.size(), cp.size());
        m.forEach((k, v) -> {
            Assert.assertTrue(cp.has(k));
            Assert.assertEquals(cp.get(k), v);
            return true;
        });

        cp.forEach((k, v) -> {
            Assert.assertTrue(m.has(k));
            Assert.assertEquals(m.get(k), v);
            return true;
        });

        Assert.assertEquals(cp, m);
        Assert.assertEquals(cp.hashCode(), m.hashCode());

        // Remove a key that does not exist.
        cp.clear(5);
        Assert.assertEquals(m, cp);

        cp.clear(2);
        Assert.assertNotEquals(m, cp);
    }

    @Test
    public void testSize() {
        final IntMap m = new IntMap(-1);
        // Empty.
        Assert.assertEquals(0, m.size());

        // Out of scope.
        m.put(-1, 3);
        Assert.assertEquals(0, m.size());

        // 2 values.
        m.put(0, 7);
        Assert.assertEquals(1, m.size());
        m.put(2, 7);
        Assert.assertEquals(2, m.size());

        // Update. No change.
        m.put(2, 3);
        Assert.assertEquals(2, m.size());

        // Clear unknown. No change.
        m.clear(7);
        Assert.assertEquals(2, m.size());

        // Clear known.
        m.clear(2);
        Assert.assertEquals(1, m.size());

        // Now unknown.
        m.clear(2);
        Assert.assertEquals(1, m.size());

        m.clear();
        Assert.assertEquals(0, m.size());
    }

    @Test
    public void testAdjust() {
        final IntMap m = new IntMap(-1);
        m.put(5, 7);
        Assert.assertEquals(m.adjust(2, 3), -1);
        Assert.assertEquals(m.adjust(-1, 3), -1);
        Assert.assertEquals(m.adjust(6, 3), -1);
        Assert.assertEquals(m.adjust(5, 3), 10);
    }
}