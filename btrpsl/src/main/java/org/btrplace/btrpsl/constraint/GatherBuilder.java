/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Gather;

import java.util.Collections;
import java.util.List;

/**
 * A builder for {@link Gather} constraints.
 *
 * @author Fabien Hermenier
 */
public class GatherBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public GatherBuilder() {
        super("gather", new ConstraintParam[]{new ListOfParam("$v", 1, BtrpOperand.Type.VM, false)});
    }

    /**
     * Build the constraint.
     *
     * @param args must be equals to one non-empty set of virtual machines.
     * @return the constraint
     */
    @Override
    public List<Gather> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
          @SuppressWarnings("unchecked")
          List<VM> vms = (List<VM>) params[0].transform(this, t, args.get(0));
            return vms != null ? Collections.singletonList(new Gather(vms)) : Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
