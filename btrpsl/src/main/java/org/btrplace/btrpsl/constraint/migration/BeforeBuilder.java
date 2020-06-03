/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint.migration;

import org.btrplace.btrpsl.constraint.ConstraintParam;
import org.btrplace.btrpsl.constraint.DefaultSatConstraintBuilder;
import org.btrplace.btrpsl.constraint.ListOfParam;
import org.btrplace.btrpsl.constraint.OneOfParam;
import org.btrplace.btrpsl.constraint.StringParam;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.migration.Deadline;
import org.btrplace.model.constraint.migration.Precedence;

import java.util.Collections;
import java.util.List;

/**
 * A builder for either {@link org.btrplace.model.constraint.migration.Precedence} or
 * {@link org.btrplace.model.constraint.migration.Deadline} constraints.
 *
 * @author Vincent Kherbache
 */
public class BeforeBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     * The parameter can be a list of VM or a string.
     */
    public BeforeBuilder() {
        super("before", new ConstraintParam[]{new ListOfParam("$vms", 1, BtrpOperand.Type.VM, false),
                new OneOfParam("$oneOf", new StringParam("$date"), new ListOfParam("$vms", 1, BtrpOperand.Type.VM, false))});
    }

    /**
     * Build a precedence constraint.
     *
     * @param t    the current tree
     * @param args can be a non-empty set of vms or a timestamp string (
     * @return a constraint
     */
    @Override
    public List<? extends SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {

        if (!checkConformance(t, args)) {
            return Collections.emptyList();
        }

        // Get the first parameter
        @SuppressWarnings("unchecked")
        List<VM> s = (List<VM>) params[0].transform(this, t, args.get(0));
        if (s == null) {
            return Collections.emptyList();
        }

        // Get param 'OneOf'
        Object obj = params[1].transform(this, t, args.get(1));
        if (obj == null) {
            return Collections.emptyList();
        }
        if (obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<VM> s2 = (List<VM>) obj;
            if (s2.isEmpty()) {
                t.ignoreError("Parameter '" + params[1].getName() + "' expects a non-empty list of VMs");
                return Collections.emptyList();
            }
            return Precedence.newPrecedence(s, s2);
        } else if (obj instanceof String) {
            String timestamp = (String) obj;
            if ("".equals(timestamp)) {
                t.ignoreError("Parameter '" + params[1].getName() + "' expects a non-empty string");
                return Collections.emptyList();
            }
            return Deadline.newDeadline(s, timestamp);
        } else {
            return Collections.emptyList();
        }
    }
}
