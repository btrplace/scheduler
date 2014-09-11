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

package btrplace.safeplace.backend;

import btrplace.safeplace.verification.TestCase;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Fabien Hermenier
 */
public class NoDuplicatedStore implements Countable {

    private BlockingQueue<TestCase> defiant;
    private BlockingQueue<TestCase> compliant;

    public NoDuplicatedStore() {
        defiant = new LinkedBlockingQueue<>();
        compliant = new LinkedBlockingQueue<>();
    }

    @Override
    public void addDefiant(TestCase c) {
        defiant.add(c);
    }

    @Override
    public void addCompliant(TestCase c) {
        compliant.add(c);
    }

    public Set<TestCase> getDefiant() {
        Set<TestCase> s = new HashSet<>();
        for (TestCase tc : defiant) {
            s.add(tc);
        }
        return s;
    }

    public Set<TestCase> getCompliant() {
        Set<TestCase> s = new HashSet<>();
        for (TestCase tc : compliant) {
            s.add(tc);
        }
        return s;
    }

    @Override
    public int getNbCompliant() {
        return getCompliant().size();
    }

    @Override
    public int getNbDefiant() {
        return getDefiant().size();
    }

    @Override
    public void flush() {
    }
}
