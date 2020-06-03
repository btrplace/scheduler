/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint.migration;

import org.btrplace.btrpsl.constraint.ConstraintParam;
import org.btrplace.btrpsl.constraint.DefaultSatConstraintBuilder;
import org.btrplace.btrpsl.constraint.ListOfParam;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.migration.Serialize;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * A builder for {@link org.btrplace.model.constraint.migration.Serialize} constraints.
 *
 * @author Vincent Kherbache
 */
public class SerializeBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public SerializeBuilder() {
        super("serialize", new ConstraintParam[]{new ListOfParam("$vms", 1, BtrpOperand.Type.VM, false)});
    }

    /**
     * Build a serialize constraint.
     *
     * @param t    the current tree
     * @param args must be 1 set of vms. The set must not be empty
     * @return  a constraint
     */
    @Override
    public List<Serialize> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (!checkConformance(t, args)) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        List<VM> s = (List<VM>) params[0].transform(this, t, args.get(0));
        if (s == null) {
            return Collections.emptyList();
        }

        if (s.size() < 2) {
            t.ignoreError("Parameter '" + params[0].getName() + "' expects a list of at least 2 VMs");
            return Collections.emptyList();
        }
        return Collections.singletonList(new Serialize(new HashSet<>(s)));
    }
}
