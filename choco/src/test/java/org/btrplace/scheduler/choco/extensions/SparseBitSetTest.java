/*
 * Copyright  2022 The BtrPlace Authors. All rights reserved.
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
        Assert.assertEquals(0, bs.cardinality());
        Assert.assertEquals(-1, bs.nextSetBit(0));
    }

    @Test
    public void testSetGet() {
        IEnvironment env = new EnvironmentTrailing();
        IStateBitSet bs = new SparseBitSet(env, 64);
        //bs.set(-1);
        // Outside.
        //Assert.assertFalse(bs.get(-1));
        // Non-existing block.
        Assert.assertFalse(bs.get(72904));
        bs.set(65);
        Assert.assertTrue(bs.get(65));
        Assert.assertFalse(bs.get(64));
        Assert.assertEquals(1, bs.cardinality());
        bs.set(0);
        Assert.assertEquals(2, bs.cardinality());
        Assert.assertTrue(bs.get(0));

        // Idempotence.
        bs.set(0, true);
        Assert.assertEquals(2, bs.cardinality());

        bs.set(0, false);
        Assert.assertEquals(1, bs.cardinality());
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
        Assert.assertEquals(0, bs.nextClearBit(0));
        bs.set(1);
        // Before the set bit, same block.
        Assert.assertEquals(0, bs.nextClearBit(0));
        // Right on the set bit. Expect the next one on this block.
        Assert.assertEquals(2, bs.nextClearBit(1));

        // Last clear bit on the same block.
        Assert.assertEquals(63, bs.nextClearBit(63));

        // On a non-existing block.
        Assert.assertEquals(135, bs.nextClearBit(135));

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
        Assert.assertEquals(3, bs.cardinality());
        bs.clear(8212);
        Assert.assertEquals(2, bs.cardinality());
        Assert.assertFalse(bs.get(8212));
        Assert.assertEquals(-1, bs.nextSetBit(8000));

        // Clear all.
        bs.clear();
        Assert.assertEquals(bs.cardinality(), 0);
        Assert.assertEquals(bs.nextSetBit(0), -1);
    }


    @DataProvider(name = "fabric")
    public Object[][] fabric() {
        final IEnvironment env = new EnvironmentTrailing();
        return new Object[][]{
                new Object[]{new S64BitSet(env)},
                new Object[]{new SparseBitSet(env, 1024)},
        };
    }

    @Test(dataProvider = "fabric")
    public void runClustering(final IStateBitSet bs) {
        int base = 100;
        int clusterSize = 32;
        int nbClusters = 400;
        int itemsPerCluster = 2000;

        long start = System.currentTimeMillis();
        for (int c = 0; c <= nbClusters; c++) {
            for (int n = 0; n < clusterSize; n++) {
                for (int i = 0; i < base; i++) {
                    bs.set(i);
                }
                for (int i = 0; i < itemsPerCluster; i++) {
                    bs.set(clusterSize * c + i);
                }
            }
        }
        System.out.println("Duration: " + (System.currentTimeMillis() - start) + "ms; bytes: " + bs.size());
    }

    @Test(dataProvider = "fabric")
    public void runRandom(final IStateBitSet bs) {
        int max = 25000;
        for (int i = 0; i < max; i++) {
            bs.set(i);
        }
        long start = System.currentTimeMillis();
        System.out.println("Duration: " + (System.currentTimeMillis() - start) + "ms; bytes: " + bs.size());
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
        Assert.assertEquals(134, prev);
        bs.set(0, 4);
        // Expand
        prev = 0;
        for (int bit = bs.nextSetBit(1); bit >= 0; bit = bs.nextSetBit(bit + 1)) {
            Assert.assertEquals(bit, prev + 1);
            prev = bit;
        }
        Assert.assertEquals(134, prev);

        bs = new SparseBitSet(env, 64);
        bs.set(38, 72);
        for (int idx = 38; idx < 72; idx++) {
            Assert.assertEquals(idx, bs.nextSetBit(idx));
        }
        Assert.assertEquals(-1, bs.nextSetBit(72));

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
        Assert.assertEquals(2, bs.cardinality());
        Assert.assertTrue(bs.get(10) && bs.get(11));
        bs.clear(10, 12);
        Assert.assertEquals(0, bs.cardinality());

        bs = new SparseBitSet(env, 64);
        bs.set(32, 257);
        bs.clear(38, 72);
        Assert.assertEquals(bs.nextSetBit(38), 72);

        // Same LB than UB. This is a nop.
        bs.clear(250, 250);
        Assert.assertTrue(bs.get(250));
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
}