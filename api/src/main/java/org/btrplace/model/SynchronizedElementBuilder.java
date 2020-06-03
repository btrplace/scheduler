/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

/**
 * A wrapper for an {@link ElementBuilder} that makes it thread-safe.
 *
 * @author Fabien Hermenier
 */
public class SynchronizedElementBuilder implements ElementBuilder {

  private final ElementBuilder base;

    private final Object vmLock;
    private final Object nodeLock;

    /**
     * Make a new builder.
     *
     * @param b the builder to wrap
     */
    public SynchronizedElementBuilder(ElementBuilder b) {
        this.base = b;
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
    public ElementBuilder copy() {
        return new SynchronizedElementBuilder(base);
    }
}
