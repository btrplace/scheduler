/*
 * Copyright  2022 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.memory.structure.S64BitSet;

import java.util.Arrays;

public class SparseBitSet implements IStateBitSet {

    /**
     * Block size in bits.
     */
    private final int blockSize;

    /**
     * The opened blocks. Every bit set states that the associated block exists on this world.
     */
    private final S64BitSet index;

    /**
     * The blocks.
     */
    private IStateBitSet[] blocks;

    /**
     * The environment to use to create internal backtrackable variables.
     */
    private final IEnvironment env;

    /**
     * @param env       backtracking environment.
     * @param blockSize block size in bits. For compactness, should be a multiple of 64.
     */
    public SparseBitSet(final IEnvironment env, final int blockSize) {
        this.env = env;
        this.blockSize = blockSize;
        blocks = new IStateBitSet[0];
        index = new S64BitSet(env);
    }

    /**
     * Get the block index for a given bit.
     * Does not ensure the block exists or the index is big enough.
     *
     * @param bit the bit
     * @return the block index.
     */
    private int index(final int bit) {
        return bit / blockSize;
    }

    /**
     * Ensure that the index is big enough.
     * If needed, the index is growth to the given value. No blocks are however created.
     *
     * @param size the desired  size.
     */
    private void ensureIndexCapacity(final int size) {
        if (size >= blocks.length) {
            blocks = Arrays.copyOf(blocks, size + 1);
        }
    }

    /**
     * Get the absolute index value.
     *
     * @param blockIdx the block index.
     * @param offset   the offset for that block.
     * @return the absolute index.
     */
    private int absolute(final int blockIdx, final int offset) {
        return blockIdx * blockSize + offset;
    }

    /**
     * Get the offset to use inside a block for a given bit.
     *
     * @param bit the bit.
     * @return the offset to use inside the selected block.
     */
    private int offset(final int bit) {
        return bit % blockSize;
    }

    /**
     * If needed, create the block at the given index.
     * The index is considered to be big enough.
     *
     * @param idx the block index.
     * @return the block at this index.
     */
    private IStateBitSet ensureBlock(final int idx) {
        if (blocks[idx] == null) {
            // Create the block and register it.
            blocks[idx] = new S64BitSet(env);
            index.set(idx);
        }
        // The block exists so the bit is expected to already be set.
        assert index.get(idx);
        return blocks[idx];
    }

    /**
     * Set the given bit
     *
     * @param bit the bit
     */
    @Override
    public void set(final int bit) {
        if (bit < 0) {
            return;
        }
        // Block index.
        final int bIdx = index(bit);
        // Ensure the index is big enough.
        ensureIndexCapacity(bIdx);
        // Set the right offset in the block.
        ensureBlock(bIdx).set(offset(bit));
    }

    @Override
    public void clear(final int bit) {
        if (bit < 0) {
            return;
        }
        // Which block.
        final int bIdx = index(bit);
        if (!index.get(bIdx)) {
            // The block is not registered in this world. Nothing to clear.
            return;
        }
        // The block exists and is registered. Clear at the offset.
        blocks[bIdx].clear(offset(bit));
    }

    @Override
    public void set(int bit, boolean flag) {
        if (flag) {
            set(bit);
        } else {
            clear(bit);
        }
    }

    @Override
    public boolean get(final int bit) {
        if (bit < 0) {
            return false;
        }
        // Which block.
        final int bIdx = index(bit);
        if (!index.get(bIdx)) {
            // Un-registered block.
            return false;
        }
        return blocks[bIdx].get(offset(bit));
    }

    @Override
    public int size() {
        int size = index.size();
        for (int bIdx = index.nextSetBit(0); bIdx >= 0; bIdx = index.nextSetBit(bIdx + 1)) {
            final IStateBitSet bs = blocks[bIdx];
            assert bs != null;
            size += bs.size();
        }
        return size;
    }

    @Override
    public int cardinality() {
        int sum = 0;
        for (int bIdx = index.nextSetBit(0); bIdx >= 0; bIdx = index.nextSetBit(bIdx + 1)) {
            final IStateBitSet bs = blocks[bIdx];
            assert bs != null;
            sum += bs.cardinality();
        }
        return sum;
    }

    @Override
    public void clear() {
        // Clear the content. The index is not considered.
        for (int bIdx = index.nextSetBit(0); bIdx >= 0; bIdx = index.nextSetBit(bIdx + 1)) {
            blocks[bIdx].clear();
        }
    }

    @Override
    public boolean isEmpty() {
        if (index.isEmpty()) {
            // No blocks so for sure an empty bitset.
            return true;
        }
        // One non-empty block is sufficient.
        for (int bIdx = index.nextSetBit(0); bIdx >= 0; bIdx = index.nextSetBit(bIdx + 1)) {
            if (!blocks[bIdx].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clear(int from, int to) {
        from = Math.max(0, from);
        // Go over every impacted blocks. The first and the last may be partially set.
        for (int bIdx = index(from); bIdx <= index(to); bIdx++) {
            if (bIdx >= blocks.length) {
                // The block is passed the index size. Thus for sure everything here is cleared.
                return;
            }
            if (!this.index.get(bIdx)) {
                // No block allocated here, nothing to clear;
                continue;
            }
            final int st;
            if (bIdx == index(from)) {
                st = from;
            } else {
                st = 0;
            }
            // local end is the block end for all the block, except the last one
            final int ed;
            if (bIdx == index(to)) {
                ed = offset(to);
            } else {
                ed = blockSize;
            }
            ensureBlock(bIdx).clear(st, ed);
        }
    }

    @Override
    public void set(int from, int to) {
        ensureIndexCapacity(index(to));
        // Go over every impacted blocks. The first and the last may be partially set.
        for (int bIdx = index(from); bIdx <= index(to); bIdx++) {
            // local start is offset(from) for the first block only, otherwise 0.
            final int st;
            if (bIdx == index(from)) {
                st = from;
            } else {
                st = 0;
            }
            // local end is the block end for all the block, except the last one
            final int ed;
            if (bIdx == index(to)) {
                ed = offset(to);
            } else {
                ed = blockSize;
            }
            ensureBlock(bIdx).set(st, ed);
        }
    }

    @Override
    public int nextSetBit(int fromIndex) {
        final int startingBlock = index(fromIndex);

        // Iterate over all the blocks starting from the current index to pick the first bit set.
        for (int bIdx = index.nextSetBit(startingBlock); bIdx >= 0; bIdx = index.nextSetBit(bIdx + 1)) {
            // For the current block, the offset is the one associated to fromIndex but at the moment the next blocks are browsed,
            // the offset is 0 to grab the first bit set.
            final int offset;
            if (bIdx > startingBlock) {
                offset = 0;
            } else {
                offset = offset(fromIndex);
            }
            int bit = blocks[bIdx].nextSetBit(offset);
            if (bit >= 0) {
                // Found it.
                return absolute(bIdx, bit);
            }
        }
        return -1;
    }

    @Override
    public int prevSetBit(int fromIndex) {
        final int lastBlockIdx = index(fromIndex);
        // Iterate over all the blocks backward, starting from the current index to pick the first bit set.
        for (int bIdx = index.prevSetBit(lastBlockIdx); bIdx >= 0; bIdx = index.prevSetBit(bIdx - 1)) {
            // For the current block, the offset is the one associated to fromIndex but at the moment the previous
            // blocks are browsed, the offset is 'blockSize' to grab the first bit set from the end.
            int offset = offset(fromIndex);
            if (bIdx < lastBlockIdx) {
                offset = blockSize;
            }
            int bit = blocks[bIdx].prevSetBit(offset);
            if (bit >= 0) {
                // Found it.
                return absolute(bIdx, bit);
            }
        }
        return -1;
    }

    @Override
    public int nextClearBit(int fromIndex) {
        final int fromBlock = index(fromIndex);
        int curBlock = fromBlock;
        while (curBlock < blocks.length) {
            if (blocks[curBlock] == null || !index.get(curBlock)) {
                // null block. fromIndex is then clear for sure.
                // In case the block is not null, check  the index in case we have set bits but a cleared block that
                // only clear the index.
                return fromIndex;
            }
            // local offset depending on the block under inspection.
            final int localOff;
            if (curBlock > fromBlock) {
                // Intermediate block, start at 0.
                localOff = 0;
            } else {
                // First block, start from fromIndex.
                localOff = offset(fromIndex);
            }
            final int nextClear = blocks[curBlock].nextClearBit(localOff);
            if (nextClear != blockSize) {
                // Not all the bits are set, the first clear bit is here.
                return absolute(curBlock, nextClear);
            }
            // All the bits are set, check the next block.
            curBlock++;
        }
        return fromIndex;
    }

    @Override
    public int prevClearBit(int fromIndex) {
        final int fromBlock = index(fromIndex);
        if (fromBlock >= index.length()) {
            // Outside the current index. For sure there is a cleared bit at fromIndex.
            return fromIndex;
        }
        int curBlock = fromBlock;
        while (curBlock >= 0) {
            if (blocks[curBlock] == null || !index.get(curBlock)) {
                // null block. fromIndex is then clear for sure. Possibly also a cleared block.
                return fromIndex;
            }
            // local offset depending on the block under inspection.
            final int localOff;
            if (curBlock > fromBlock) {
                // Intermediate block, start at blockSize.
                localOff = blockSize;
            } else {
                // First block, start from fromIndex.
                localOff = offset(fromIndex);
            }
            final int nextClear = blocks[curBlock].nextClearBit(localOff);
            if (nextClear != blockSize) {
                // Not all the bits are set, the first clear bit is here.
                return absolute(curBlock, nextClear);
            }
            // All the bits are set, check the next block.
            curBlock++;
        }
        return fromIndex;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append('{');
        int i = nextSetBit(0);
        if (i != -1) {
            b.append(i);
            for (i = nextSetBit(i + 1); i >= 0; i = nextSetBit(i + 1)) {
                b.append(", ").append(i);
            }
        }
        b.append('}');
        return b.toString();
    }
}
