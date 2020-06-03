/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Lonely;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * A builder for {@link Lonely} constraints.
 *
 * @author Fabien Hermenier
 */
public class LonelyBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public LonelyBuilder() {
        super("lonely", new ConstraintParam[]{new ListOfParam("$v", 1, BtrpOperand.Type.VM, false)});
    }

    /**
     * Build a constraint.
     *
     * @param args the parameters of the constraint. Must be one non-empty set of virtual machines.
     * @return the constraint
     */
    @Override
    public List<Lonely> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
          @SuppressWarnings("unchecked")
          List<VM> vms = (List<VM>) params[0].transform(this, t, args.get(0));
            return vms != null ? Collections.singletonList(new Lonely(new HashSet<>(vms))) : Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
