/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.type;

import org.btrplace.safeplace.spec.term.Constant;

/**
 * @author Fabien Hermenier
 */
public class VMStateType implements Litteral, Atomic {

  public enum Type {READY, BOOTING, RUNNING, MIGRATING, SUSPENDING, SLEEPING, RESUMING, TERMINATED}

  private static final VMStateType instance = new VMStateType();

    private VMStateType() {
    }

    public static VMStateType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "vmState";
    }

    @Override
    @SuppressWarnings("squid:S1166")
    public Constant parse(String n) {
        try {
            return new Constant(Type.valueOf(n.toUpperCase()), this);
        } catch (@SuppressWarnings("unused") IllegalArgumentException ex) {
            return null;
        }
    }

}
