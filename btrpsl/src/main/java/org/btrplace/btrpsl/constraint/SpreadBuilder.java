/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Spread;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A builder for {@link Spread} constraints.
 *
 * @author Fabien Hermenier
 */
public class SpreadBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public SpreadBuilder() {
        super("spread", new ConstraintParam[]{new ListOfParam("$v", 1, BtrpOperand.Type.VM, false)});
    }

    @Override
    public List<Spread> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
            @SuppressWarnings("unchecked")
            List<VM> vms = (List<VM>) params[0].transform(this, t, args.get(0));
            if (vms == null) {
                return Collections.emptyList();
            }
            Set<VM> s = new HashSet<>(vms);
            if (s.size() != vms.size()) {
                return Collections.emptyList();
            }
            return Collections.singletonList(new Spread(new HashSet<>(vms), true));
        }
        return Collections.emptyList();
    }
}
