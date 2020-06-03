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
import org.btrplace.model.constraint.MaxOnline;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A builder for {@link org.btrplace.model.constraint.MaxOnline} constraints.
 *
 * @author Fabien Hermenier
 */
public class MaxOnlineBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public MaxOnlineBuilder() {
        super("maxOnline", new ConstraintParam[]{new ListOfParam("$n", 1, BtrpOperand.Type.NODE, false), new NumberParam("$nb")});
    }

    /**
     * Build an online constraint.
     *
     * @param args must be 1 set of nodes. The set must not be empty
     * @return a constraint
     */
    @Override
    public List<MaxOnline> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
          @SuppressWarnings("unchecked")
          List<Node> ns = (List<Node>) params[0].transform(this, t, args.get(0));
            if (ns == null) {
                return Collections.emptyList();
            }
            Set<Node> s = new HashSet<>(ns);
            if (s.size() != ns.size()) {                //Prevent duplicates
                return Collections.emptyList();
            }

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

            return Collections.singletonList(new MaxOnline(new HashSet<>(ns), v, true));
        }
        return Collections.emptyList();
    }
}
