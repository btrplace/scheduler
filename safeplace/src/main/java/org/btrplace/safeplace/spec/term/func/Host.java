/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.safeplace.spec.type.NodeType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.spec.type.VMType;
import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * @author Fabien Hermenier
 */
public class Host implements Function<Node> {

    @Override
    public String id() {
        return "host";
    }

    @Override
    public NodeType type() {
        return NodeType.getInstance();
    }

    @Override
    public Node eval(Context mo, Object... args) {
        VM vm = (VM) args[0];
        if (vm == null) {
            throw new UnsupportedOperationException();
        }
        return mo.getMapping().host(vm);
    }

    @Override
    public Type[] signature() {
        return new Type[]{VMType.getInstance()};
    }
}
