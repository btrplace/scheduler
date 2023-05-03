/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.memory.structure.S64BitSet;
import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.chocosolver.solver.Model;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Random;

public class SparseBitSetTest {

    @Test
    public void testNew() {
        IEnvironment env = new EnvironmentTrailing();
        IStateBitSet bs = new SparseBitSet(env, 64);
        Assert.assertTrue(bs.isEmpty());
        Assert.assertEquals(bs.cardinality(), 0);
        Assert.assertEquals(bs.nextSetBit(0), -1);
    }

    @Test
    public void testSetGet() {
        IEnvironment env = new EnvironmentTrailing();
        IStateBitSet bs = new SparseBitSet(env, 64);
        // Non-existing block.
        Assert.assertFalse(bs.get(72904));
        bs.set(65);
        Assert.assertTrue(bs.get(65));
        Assert.assertFalse(bs.get(64));
        Assert.assertEquals(bs.cardinality(), 1);
        bs.set(0);
        Assert.assertEquals(bs.cardinality(), 2);
        Assert.assertTrue(bs.get(0));

        // Idempotence.
        bs.set(0, true);
        Assert.assertEquals(bs.cardinality(), 2);

        bs.set(0, false);
        Assert.assertEquals(bs.cardinality(), 1);
        Assert.assertFalse(bs.get(0));
    }

    @Test
    public void testNextSetBit() {
        IEnvironment env = new EnvironmentTrailing();
        IStateBitSet bs = new SparseBitSet(env, 64);
        // Set inside the first block.
        bs.set(1);
        // Other consecutive bit sets on different blocks.
        bs.set(255);
        bs.set(256);
        bs.set(257);

        // Right on the spot
        Assert.assertEquals(bs.nextSetBit(1), 1);
        Assert.assertEquals(bs.nextSetBit(255), 255);
        Assert.assertEquals(bs.nextSetBit(256), 256);
        Assert.assertEquals(bs.nextSetBit(257), 257);

        // Before, same block.
        Assert.assertEquals(bs.nextSetBit(0), 1);
        // Before previous blocks.
        Assert.assertEquals(bs.nextSetBit(128), 255);
        // After, used block.
        Assert.assertEquals(bs.nextSetBit(258), -1);
        // After, unused block.
        Assert.assertEquals(bs.nextSetBit(65445), -1);
    }

    @Test
    public void testNextClearBit() {
        final IEnvironment env = new EnvironmentTrailing();
        IStateBitSet bs = new SparseBitSet(env, 64);
        Assert.assertEquals(bs.nextClearBit(0), 0);
        bs.set(1);
        // Before the set bit, same block.
        Assert.assertEquals(bs.nextClearBit(0), 0);
        // Right on the set bit. Expect the next one on this block.
        Assert.assertEquals(bs.nextClearBit(1), 2);

        // Last clear bit on the same block.
        Assert.assertEquals(bs.nextClearBit(63), 63);

        // On a non-existing block.
        Assert.assertEquals(bs.nextClearBit(135), 135);

        // Create a range of set bits over multiple blocks.
        bs.set(32, 257);
        bs.clear(38, 72); // clear From 38 to 71 (incl)
        Assert.assertEquals(bs.nextClearBit(37), 38);
        Assert.assertEquals(bs.nextClearBit(71), 71);
        Assert.assertEquals(bs.nextClearBit(72), 257);
        Assert.assertEquals(bs.nextClearBit(65456), 65456);
    }

    @Test
    public void testPrevClearBit() {
        final IEnvironment env = new EnvironmentTrailing();
        final IStateBitSet bs = new SparseBitSet(env, 64);
        Assert.assertEquals(bs.prevClearBit(0), 0);
        bs.set(0);
        Assert.assertEquals(bs.prevClearBit(0), -1);
        Assert.assertEquals(bs.prevClearBit(192), 192);
        bs.set(32, 257);
        Assert.assertEquals(bs.prevClearBit(256), 31);
    }

    @Test
    public void testPrevSetBit() {
        IEnvironment env = new EnvironmentTrailing();
        IStateBitSet bs = new SparseBitSet(env, 64);
        // Set inside the first block.
        bs.set(1);
        // Other consecutive bit sets on different blocks
        bs.set(255);
        bs.set(256);
        bs.set(257);

        // Right on the spot
        Assert.assertEquals(bs.prevSetBit(1), 1);
        Assert.assertEquals(bs.prevSetBit(255), 255);
        Assert.assertEquals(bs.prevSetBit(256), 256);
        Assert.assertEquals(bs.prevSetBit(257), 257);

        // After, same block.
        Assert.assertEquals(bs.prevSetBit(2), 1);
        // After next blocks.
        Assert.assertEquals(bs.prevSetBit(260), 257);
        // After, unused block.
        Assert.assertEquals(bs.prevSetBit(65445), 257);

        Assert.assertEquals(-1, bs.prevSetBit(0));
    }

    @Test
    public void testClear() {
        IEnvironment env = new EnvironmentTrailing();
        IStateBitSet bs = new SparseBitSet(env, 64);
        bs.set(1);
        bs.set(128);
        bs.set(8212);
        bs.clear(54);
        Assert.assertEquals(bs.cardinality(), 3);
        bs.clear(8212);
        Assert.assertEquals(bs.cardinality(), 2);
        Assert.assertFalse(bs.get(8212));
        Assert.assertEquals(-1, bs.nextSetBit(8000));

        // Clear all.
        bs.clear();
        Assert.assertEquals(bs.cardinality(), 0);
        Assert.assertEquals(bs.nextSetBit(0), -1);
    }

    @DataProvider(name = "invalidRanges")
    public Object[][] invalidRanges() {
        return new Object[][]{{-1, 3}, {5, 4}, {-1, -1}};
    }

    @Test(dataProvider = "invalidRanges", expectedExceptions = IndexOutOfBoundsException.class)
    public void testBadSetRanges(final int from, final int to) {
        final IEnvironment env = new EnvironmentTrailing();
        final IStateBitSet bs = new SparseBitSet(env, 64);
        bs.set(from, to);
    }

    @Test(dataProvider = "invalidRanges", expectedExceptions = IndexOutOfBoundsException.class)
    public void testBadClearRanges(final int from, final int to) {
        final IEnvironment env = new EnvironmentTrailing();
        final IStateBitSet bs = new SparseBitSet(env, 64);
        bs.clear(from, to);
    }

    @Test
    public void testSetRange() {
        IEnvironment env = new EnvironmentTrailing();
        IStateBitSet bs = new SparseBitSet(env, 64);
        IStateBitSet ref = new S64BitSet(env);

        bs.set(3, 135);
        ref.set(3, 135);
        Assert.assertEquals(bs.cardinality(), 132);
        int prev = 3;
        for (int bit = bs.nextSetBit(4); bit >= 0; bit = bs.nextSetBit(bit + 1)) {
            Assert.assertEquals(bit, prev + 1);
            prev = bit;
        }
        Assert.assertEquals(prev, 134);
        bs.set(0, 4);
        // Expand
        prev = 0;
        for (int bit = bs.nextSetBit(1); bit >= 0; bit = bs.nextSetBit(bit + 1)) {
            Assert.assertEquals(bit, prev + 1);
            prev = bit;
        }
        Assert.assertEquals(prev, 134);

        bs = new SparseBitSet(env, 64);
        bs.set(38, 72);
        for (int idx = 38; idx < 72; idx++) {
            Assert.assertEquals(idx, bs.nextSetBit(idx));
        }
        Assert.assertEquals(bs.nextSetBit(72), -1);

        // UB and LB are the same, that is a nop.
        bs.set(155, 155);
        Assert.assertFalse(bs.get(155));
    }

    @Test
    public void testMemorySize() {
        final IEnvironment env = new EnvironmentTrailing();
        final IStateBitSet ref = new S64BitSet(env);
        final SparseBitSet sparse = new SparseBitSet(env, 64);
        ref.set(0);
        sparse.set(0);
        Assert.assertEquals(ref.size(), 64);
        // One long for the index, one for the block. There is an overhead compared to S64BitSet.
        Assert.assertEquals(sparse.memorySize(), 2 * 64);

        sparse.set(128 * 64 - 1);
        ref.set(128 * 64 - 1);
        Assert.assertEquals(ref.size(), 128 * 64);
        // 2 longs for the index, 2 longs for bit 0 and 128*64. Compared to size(), we see the compaction.
        Assert.assertEquals(sparse.memorySize(), 4 * 64);
        Assert.assertEquals(sparse.size(), 128 * 64);
    }

    @Test
    public void testSize() {
        final IEnvironment env = new EnvironmentTrailing();
        final IStateBitSet sparse = new SparseBitSet(env, 64);
        sparse.set(0);
        // 64 blocks of 64 bits each.
        Assert.assertEquals(sparse.size(), 64 * 64);
        sparse.set(128 * 64 - 1);
        // 128 blocks of 128 bits each.
        Assert.assertEquals(sparse.size(), 128 * 64);
    }

    @Test
    public void testClearRange() {
        IEnvironment env = new EnvironmentTrailing();
        IStateBitSet bs = new SparseBitSet(env, 64);
        bs.clear(0, 187);
        Assert.assertEquals(bs.cardinality(), 0);
        bs.set(10, 237);
        bs.clear(12, 239);
        Assert.assertEquals(bs.cardinality(), 2);
        Assert.assertTrue(bs.get(10) && bs.get(11));
        bs.clear(10, 12);
        Assert.assertEquals(bs.cardinality(), 0);

        bs = new SparseBitSet(env, 64);
        bs.set(32, 257);
        bs.clear(38, 72);
        Assert.assertEquals(bs.nextSetBit(38), 72);

        // Same LB than UB. This is a nop.
        bs.clear(250, 250);
        Assert.assertTrue(bs.get(250));

        bs = new SparseBitSet(env, 32);
        bs.set(0, 32);
        bs.set(64, 70);
        // Clear where there is no blocks.
        bs.clear(32, 64);
        Assert.assertEquals(bs.nextSetBit(64), 64);
    }

    @Test
    public void testEmpty() {
        IEnvironment env = new EnvironmentTrailing();
        IStateBitSet bs = new SparseBitSet(env, 64);
        Assert.assertTrue(bs.isEmpty());
        bs.set(10, 237);
        Assert.assertFalse(bs.isEmpty());
        bs.clear(10, 237);
        Assert.assertTrue(bs.isEmpty());
    }

    @Test
    public void testBacktracking() {
        final Model mo = new Model(new EnvironmentTrailing(), "foo");
        final IEnvironment env = mo.getEnvironment();

        final IStateBitSet ref = env.makeBitSet(1000);
        final SparseBitSet test = new SparseBitSet(env, 64);
        Random rnd = new Random();
        // Set some.
        for (int i = 0; i < 100; i++) {
            int bit = rnd.nextInt(1010);
            ref.set(bit);
            test.set(bit);
        }
        env.worldPush();
        checkEquivalence(ref, test, 1010);
        for (int i = 0; i < 100; i++) {
            int bit = rnd.nextInt(1010);
            ref.clear(bit);
            test.clear(bit);
        }
        checkEquivalence(ref, test, 1010);
        env.worldPop();
        checkEquivalence(ref, test, 1010);

        ref.clear();
        test.clear();
        checkEquivalence(ref, test, 1010);
        env.worldPush();
        ref.set(0, 100);
        test.set(0, 100);
        checkEquivalence(ref, test, 1010);
        env.worldPop();
        checkEquivalence(ref, test, 1010);
    }

    public void checkEquivalence(final IStateBitSet ref, final SparseBitSet got, int max) {
        // Check that the two bitsets are equivalent. This consists in checking every public APIs.
        Assert.assertEquals(ref.cardinality(), got.cardinality());
        Assert.assertEquals(ref.isEmpty(), got.isEmpty());
        // Iteration capabilities.
        for (int i = max; i >= 0; i--) {
            Assert.assertEquals(ref.prevSetBit(i), got.prevSetBit(i));
            Assert.assertEquals(ref.nextSetBit(i), got.nextSetBit(i));
            Assert.assertEquals(ref.nextClearBit(i), got.nextClearBit(i));
            Assert.assertEquals(ref.prevClearBit(i), got.prevClearBit(i));
        }
        Assert.assertEquals(ref.toString(), got.toString());
    }

    @Test
    public void testEquals() {
        final IEnvironment env = new EnvironmentTrailing();
        final IStateBitSet ref = new S64BitSet(env, 64);
        final IStateBitSet sparse = new SparseBitSet(env, 128);

        // S64Bitset.equals is not correct, cannot use Assert.assertEquals()
        Assert.assertTrue(sparse.equals(ref));
        ref.set(7, 32);
        sparse.set(7, 32);

        Assert.assertTrue(sparse.equals(ref));
        ref.set(278, 317);
        sparse.set(278, 317);

        Assert.assertTrue(sparse.equals(ref));

        env.worldPush();
        // Clear a bit already cleared. No change expected.
        sparse.clear(3);
        Assert.assertTrue(sparse.equals(ref));

        // Bit cleared in a new world. No longer equals.
        sparse.clear(300);
        Assert.assertFalse(sparse.equals(ref));

        // Restore the event.
        env.worldPop();
        Assert.assertTrue(sparse.equals(ref));

        // Same cardinality, not the same bits.
        sparse.clear(300);
        sparse.set(1);
        Assert.assertFalse(sparse.equals(ref));

        Assert.assertEquals(sparse, sparse);
        Assert.assertFalse(sparse.equals(null));
    }

    @Test
    public void testHashCode() {
        final IEnvironment env = new EnvironmentTrailing();
        // Different block sizes but same content.
        final IStateBitSet sparse1 = new SparseBitSet(env, 128);
        final IStateBitSet sparse2 = new SparseBitSet(env, 64);
        Assert.assertEquals(sparse1.hashCode(), sparse2.hashCode());
        sparse1.set(1);
        sparse1.set(64);
        sparse1.set(137);

        sparse2.set(1);
        sparse2.set(64);
        sparse2.set(137);
        Assert.assertEquals(sparse1.hashCode(), sparse2.hashCode());
        sparse2.clear(1);
        Assert.assertNotEquals(sparse1.hashCode(), sparse2.hashCode());
    }
}