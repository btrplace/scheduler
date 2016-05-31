/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
package org.btrplace.scheduler.choco.extensions.env;


import org.btrplace.scheduler.choco.extensions.env.trail.*;
import org.btrplace.scheduler.choco.extensions.env.trail.chuncked.ChunkedDoubleTrail;
import org.btrplace.scheduler.choco.extensions.env.trail.chuncked.ChunkedIntTrail;
import org.btrplace.scheduler.choco.extensions.env.trail.chuncked.ChunkedLongTrail;
import org.btrplace.scheduler.choco.extensions.env.trail.flatten.FlatBoolTrail;
import org.btrplace.scheduler.choco.extensions.env.trail.flatten.FlatOperationTrail;
import org.chocosolver.memory.*;
import org.chocosolver.memory.structure.Operation;

/**
 * A trailing environment that allocate memory per world.
 * It tends to increase the memory usage but reduces significantly the solving duration
 * when the number of nodes and VMs is very large.
 *
 * @author Fabien Hermenier
 */
public final class ChunkedTrailing extends AbstractEnvironment {

    private IntTrail intTrail;
    private BoolTrail boolTrail;
    private LongTrail longTrail;
    private DoubleTrail doubleTrail;
    private StoredIntVectorTrail intVectorTrail;
    private OperationTrail operationTrail;

    /**
     * Contains all the {@link org.chocosolver.memory.IStorage} trails for
     * storing different kinds of data.
     */
    private TraceableStorage[] trails;

    /**
     * Constructs a new <code>IEnvironment</code> with
     * the default stack sizes : 50000 and 1000.
     */
    public ChunkedTrailing() {
        super(Type.FLAT);
        trails = new TraceableStorage[6];
        intTrail = new ChunkedIntTrail(1024, 1024);
        boolTrail = new FlatBoolTrail(128, 1024);
        longTrail = new ChunkedLongTrail(1024, 1024);
        doubleTrail = new ChunkedDoubleTrail(1024, 1024);
        operationTrail = new FlatOperationTrail(1000, 5000);
        intVectorTrail = new StoredIntVectorTrail(this, 1024, 1000);
        trails[0] = intTrail;
        trails[1] = boolTrail;
        trails[2] = longTrail;
        trails[3] = doubleTrail;
        trails[4] = operationTrail;
        trails[5] = intVectorTrail;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void worldPush() {
        timestamp++;
        final int wi = currentWorld + 1;
        for (int i = trails.length - 1; i >= 0; i--) {
            trails[i].worldPush(wi);
        }
        currentWorld++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void worldPop() {
        timestamp++;
        for (int i = trails.length - 1; i >= 0; i--) {
            trails[i].worldPop(currentWorld);
        }
        currentWorld--;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void worldCommit() {
        if (currentWorld == 0) {
            throw new IllegalStateException("Commit in world 0?");
        }
        for (int i = trails.length; i >= 0; i--) {
            trails[i].worldCommit(currentWorld);
        }
        currentWorld--;
    }

    @Override
    public IStateInt makeInt() {
        return makeInt(0);
    }

    @Override
    public IStateInt makeInt(final int initialValue) {
        return new StoredInt(this, initialValue);
    }

    @Override
    public IStateBool makeBool(final boolean initialValue) {
        return new StoredBool(this, initialValue);
    }

    @Override
    public IStateIntVector makeIntVector(final int size, final int initialValue) {
        return new StoredIntVector(this, size, initialValue);
    }

    @Override
    public IStateDoubleVector makeDoubleVector(final int size, final double initialValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IStateDouble makeFloat() {
        return makeFloat(Double.NaN);
    }

    @Override
    public IStateDouble makeFloat(final double initialValue) {
        return new StoredDouble(this, initialValue);
    }

    @Override
    public IStateLong makeLong() {
        return makeLong(0);
    }

    @Override
    public IStateLong makeLong(final long init) {
        return new StoredLong(this, init);
    }


    /**
     * Get the trail for backtrackable integers.
     *
     * @return a trail
     */
    public IntTrail getIntTrail() {
        return intTrail;
    }

    /**
     * Get the trail for backtrackable longs.
     * @return a trail
     */
    public LongTrail getLongTrail() {
        return longTrail;
    }

    /**
     * Get the trail for backtrackable booleans.
     * @return a trail
     */
    public BoolTrail getBoolTrail() {
        return boolTrail;
    }

    /**
     * Get the trail for backtrackable doubles.
     * @return a trail
     */
    public DoubleTrail getDoubleTrail() {
        return doubleTrail;
    }

    /**
     * Get the trail for backtrackable operations.
     * @return a trail
     */
    public OperationTrail getOperationTrail() {
        return operationTrail;
    }

    /**
     * Get the trail for backtrackable vector of integers.
     * @return a trail
     */
    public StoredIntVectorTrail getIntVectorTrail() {
        return intVectorTrail;
    }

    /**
     * Get the trail for backtrackable vector of doubles.
     * @return a trail
     */
    public StoredDoubleVectorTrail getDoubleVectorTrail() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(Operation oldValue) {
        getOperationTrail().savePreviousState(oldValue);
    }

    /**
     * Print statistics for each trails regarding to their memory usage.
     *
     * @return a String
     */
    public String statistics() {
        StringBuilder b = new StringBuilder();
        for (IStorage s : trails) {
            b.append(s.getClass().getSimpleName()).append(": ").append(((TraceableStorage) s).allocated()).append("\n");
        }
        return b.toString();
    }
}

