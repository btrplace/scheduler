/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.model.Node;
import org.btrplace.safeplace.spec.type.NodeStateType;
import org.btrplace.safeplace.spec.type.NodeType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * @author Fabien Hermenier
 */
public class NodeState implements Function<NodeStateType.Type> {

    @Override
    public NodeStateType type() {
        return NodeStateType.getInstance();
    }

    @Override
    public NodeStateType.Type eval(Context mo, Object... args) {
        Node n = (Node) args[0];
        if (n == null) {
            return null;
        }
        return mo.getMapping().state(n);
    }

    @Override
    public String id() {
        return "nodeState";
    }

    @Override
    public Type[] signature() {
        return new Type[]{NodeType.getInstance()};
    }
}
