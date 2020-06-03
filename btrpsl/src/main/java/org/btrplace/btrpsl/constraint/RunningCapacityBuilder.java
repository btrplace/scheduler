/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.element.BtrpNumber;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;
import org.btrplace.model.Node;
import org.btrplace.model.constraint.RunningCapacity;
import org.btrplace.model.constraint.SatConstraint;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * A builder for {@link org.btrplace.model.constraint.RunningCapacity} constraints.
 *
 * @author Fabien Hermenier
 */
public class RunningCapacityBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public RunningCapacityBuilder() {
        super("runningCapacity", new ConstraintParam[]{new ListOfParam("$n", 1, BtrpOperand.Type.NODE, false), new NumberParam("$nb")});
    }

    @Override
    public List<? extends SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (!checkConformance(t, args)) {
            return Collections.emptyList();
        }
        @SuppressWarnings("unchecked")
        List<Node> ns = (List<Node>) params[0].transform(this, t, args.get(0));
        BtrpNumber n = (BtrpNumber) args.get(1);
        if (!n.isInteger()) {
            t.ignoreError("Parameter '" + params[1].getName() + "' expects an integer");
            return Collections.emptyList();
        }
        int v = n.getIntValue();
        if (v < 0) {
            t.ignoreError("Parameter '" + params[1].getName() + "' expects a positive integer (" + v + " given)");
            return Collections.emptyList();
        }

        return ns != null ?
                Collections.singletonList(new RunningCapacity(new HashSet<>(ns), v)) :
                Collections.emptyList();
    }
}
