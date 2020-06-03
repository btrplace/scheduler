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
public class NodeStateType implements Litteral, Atomic {

    public enum Type {ONLINE, BOOTING, HALTING, OFFLINE}

  private static final NodeStateType instance = new NodeStateType();

    private NodeStateType() {
    }

    public static NodeStateType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "nodeState";
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
