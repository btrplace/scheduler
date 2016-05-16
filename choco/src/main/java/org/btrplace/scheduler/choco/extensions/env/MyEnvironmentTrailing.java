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
import org.btrplace.scheduler.choco.extensions.env.trail.chuncked.ChunkedBoolTrail;
import org.btrplace.scheduler.choco.extensions.env.trail.chuncked.ChunkedDoubleTrail;
import org.btrplace.scheduler.choco.extensions.env.trail.chuncked.ChunkedIntTrail;
import org.btrplace.scheduler.choco.extensions.env.trail.chuncked.ChunkedLongTrail;
import org.btrplace.scheduler.choco.extensions.env.trail.chuncked.ChunkedOperationTrail;
import org.btrplace.scheduler.choco.extensions.env.trail.flatten.FlatBoolTrail;
import org.chocosolver.memory.*;
import org.chocosolver.memory.structure.Operation;
import org.chocosolver.memory.trailing.trail.IStoredBoolTrail;

/**
 * The root class for managing memory and sessions.
 * <p/>
 * A environment is associated to each problem.
 * It is responsible for managing backtrackable data.
 */
public final class MyEnvironmentTrailing extends AbstractEnvironment {

    //Contains all the {@link IStorage} trails for
    // storing different kinds of data.

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
    private IStorage[] trails;

    /**
     * Constructs a new <code>IEnvironment</code> with
     * the default stack sizes : 50000 and 1000.
     */

    public MyEnvironmentTrailing() {
        super(Type.FLAT);
        System.out.println("custom trailing");
        trails = new IStorage[6];
        intTrail = new ChunkedIntTrail(5000);
        boolTrail = new FlatBoolTrail(5000, 100);
        longTrail = new ChunkedLongTrail(5000);
        doubleTrail = new ChunkedDoubleTrail();
        operationTrail = new ChunkedOperationTrail();
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
        for (int i = 0; i < trails.length; i++) {
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
        final int wi = currentWorld;
        for (int i = trails.length - 1; i >= 0; i--) {
            trails[i].worldPop(wi);
        }
        currentWorld--;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void worldCommit() {
        //code optim.: replace loop by enumeration;
        if (currentWorld == 0) {
            throw new IllegalStateException("Commit in world 0?");
        }
        final int wi = currentWorld;
        for (int i = trails.length; i >= 0; i--) {
            trails[i].worldCommit(wi);
        }
        currentWorld--;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateInt makeInt() {
        return makeInt(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateInt makeInt(final int initialValue) {
        return new StoredInt(this, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateBool makeBool(final boolean initialValue) {
        return new StoredBool(this, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateIntVector makeIntVector(final int size, final int initialValue) {
        return new StoredIntVector(this, size, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateDoubleVector makeDoubleVector(final int size, final double initialValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateDouble makeFloat() {
        return makeFloat(Double.NaN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateDouble makeFloat(final double initialValue) {
        return new StoredDouble(this, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateLong makeLong() {
        return makeLong(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateLong makeLong(final long init) {
        return new StoredLong(this, init);
    }


    public IntTrail getIntTrail() {
        return intTrail;
    }

    public LongTrail getLongTrail() {
        return longTrail;
    }

    public BoolTrail getBoolTrail() {
        return boolTrail;
    }

    public DoubleTrail getDoubleTrail() {
        return doubleTrail;
    }

    public OperationTrail getOperationTrail() {
        return operationTrail;
    }

    public StoredIntVectorTrail getIntVectorTrail() {
        return intVectorTrail;
    }

    public StoredDoubleVectorTrail getDoubleVectorTrail() {
        throw new UnsupportedOperationException();
    }
    
    public void save(Operation oldValue) {
        getOperationTrail().savePreviousState(oldValue);
    }
}

