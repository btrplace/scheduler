/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.safeplace.spec.type.IntType;
import org.btrplace.safeplace.spec.type.StringType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.spec.type.VMType;
import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * @author Fabien Hermenier
 */
public class Cons implements Function<Integer> {

    @Override
    public Integer eval(Context mo, Object... args) {
        String rc = (String) args[1];
        ShareableResource r = ShareableResource.get(mo.getModel(), rc);
        if (r == null) {
            throw new IllegalArgumentException("View '" + rc + "' is missing");
        }
        return r.getConsumption((VM) args[0]);
    }

    @Override
    public Type type() {
        return IntType.getInstance();
    }

    @Override
    public String id() {
        return "cons";
    }

    @Override
    public Type[] signature() {
        return new Type[]{VMType.getInstance(), StringType.getInstance()};
    }
}
