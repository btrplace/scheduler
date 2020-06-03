/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Ready;
import org.btrplace.model.constraint.SatConstraint;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A builder for {@link Ready} constraints.
 *
 * @author Fabien Hermenier
 */
public class ReadyBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public ReadyBuilder() {
        super("ready", new ConstraintParam[]{new ListOfParam("$vms", 1, BtrpOperand.Type.VM, false)});
    }

    /**
     * Build an online constraint.
     *
     * @param args must be 1 set of vms. The set must not be empty
     * @return a constraint
     */
    @Override
    public List<SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
            @SuppressWarnings("unchecked")
            List<VM> s = (List<VM>) params[0].transform(this, t, args.get(0));
            if (s == null) {
                return Collections.emptyList();
            }
            return s.stream().map(Ready::new).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
