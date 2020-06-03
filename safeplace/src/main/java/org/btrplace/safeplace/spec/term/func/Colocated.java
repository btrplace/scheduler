/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.safeplace.spec.type.SetType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.spec.type.VMType;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Colocated implements Function<Set<VM>> {

    @Override
    public SetType type() {
        return new SetType(VMType.getInstance());
    }

    @Override
    public Set<VM> eval(Context mo, Object... args) {
        VM v = (VM) args[0];
        if (v == null) {
            return null;
        }
        Node n = mo.getMapping().host(v);
        if (n == null) {
            return new HashSet<>();
        }
        Set<VM> vms = new HashSet<>();
        vms.addAll(mo.getMapping().sleeping(n));
        vms.addAll(mo.getMapping().runnings(n));
        return vms;
    }

    @Override
    public String id() {
        return "colocated";
    }

    @Override
    public Type[] signature() {
        return new Type[]{VMType.getInstance()};
    }
}
