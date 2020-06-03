/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.model.Node;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.safeplace.spec.type.IntType;
import org.btrplace.safeplace.spec.type.NodeType;
import org.btrplace.safeplace.spec.type.StringType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * @author Fabien Hermenier
 */
public class Capa implements Function<Integer> {

    @Override
    public String id() {
        return "capa";
    }

    @Override
    public Integer eval(Context mo, Object... args) {
        String rc = (String) args[1];
        ShareableResource r = ShareableResource.get(mo.getModel(), rc);
        if (r == null) {
            throw new IllegalArgumentException("View '" + rc + "' is missing");
        }
        return r.getCapacity((Node) args[0]);

    }

    @Override
    public Type[] signature() {
        return new Type[]{NodeType.getInstance(), StringType.getInstance()};
    }


    @Override
    public Type type() {
        return IntType.getInstance();
    }
}
