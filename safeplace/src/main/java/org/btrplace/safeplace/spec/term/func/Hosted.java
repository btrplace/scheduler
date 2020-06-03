/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.safeplace.spec.type.NodeType;
import org.btrplace.safeplace.spec.type.SetType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.spec.type.VMType;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Hosted implements Function<Set<VM>> {

    @Override
    public SetType type() {
        return new SetType(VMType.getInstance());
    }

    @Override
    public Set<VM> eval(Context mo, Object... args) {
        Node n = (Node) args[0];
        if (n == null) {
            throw new UnsupportedOperationException();
        }
        return mo.getMapping().hosted(n);
    }

    @Override
    public String id() {
        return "hosted";
    }

    @Override
    public Type[] signature() {
        return new Type[]{NodeType.getInstance()};
    }
}
