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

/**
 * A wrapper for an {@link ElementBuilder} that makes it thread-safe.
 *
 * @author Fabien Hermenier
 */
public class SynchronizedElementBuilder implements ElementBuilder {

    private ElementBuilder base;

    private final Object vmLock, nodeLock;

    /**
     * Make a new builder.
     *
     * @param base the builder to wrap
     */
    public SynchronizedElementBuilder(ElementBuilder base) {
        this.base = base;
        vmLock = new Object();
        nodeLock = new Object();
    }

    @Override
    public VM newVM() {
        synchronized (vmLock) {
            return base.newVM();
        }
    }

    @Override
    public VM newVM(int id) {
        synchronized (vmLock) {
            return base.newVM(id);
        }
    }

    @Override
    public Node newNode() {
        synchronized (nodeLock) {
            return base.newNode();
        }
    }

    @Override
    public Node newNode(int id) {
        synchronized (nodeLock) {
            return base.newNode(id);
        }

    }

    @Override
    public boolean contains(VM v) {
        synchronized (vmLock) {
            return base.contains(v);
        }
    }

    @Override
    public boolean contains(Node n) {
        synchronized (nodeLock) {
            return base.contains(n);
        }
    }

    @Override
    public ElementBuilder clone() {
        return new SynchronizedElementBuilder(base);
    }
}
