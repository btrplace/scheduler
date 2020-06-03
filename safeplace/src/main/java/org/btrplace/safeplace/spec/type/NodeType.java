/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.type;

import org.btrplace.model.Node;

/**
 * @author Fabien Hermenier
 */
public class NodeType implements Atomic {

    private static final NodeType instance = new NodeType();


    private NodeType() {
    }

    public static NodeType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "node";
    }

    @Override
    public String encode() {
        return label();
    }

    @Override
    public Object toJSON(Object value) {
        return ((Node)value).id();
    }

    @Override
    public Node fromJSON(Object value) {
        return new Node((Integer) value);
    }

}
