/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Killed;

import java.util.Collections;
import java.util.List;

/**
 * A builder for {@link Killed} constraints.
 *
 * @author Fabien Hermenier
 */
public class KilledBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public KilledBuilder() {
        super("killed", new ConstraintParam[]{new ListOfParam("$vms", 1, BtrpOperand.Type.VM, false)});
    }

    /**
     * Build an online constraint.
     *
     * @param args must be 1 set of vms. The set must not be empty
     * @return a constraint
     */
    @Override
    public List<Killed> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
          @SuppressWarnings("unchecked")
          List<VM> s = (List<VM>) params[0].transform(this, t, args.get(0));
            return s != null ? Killed.newKilled(s) : Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
