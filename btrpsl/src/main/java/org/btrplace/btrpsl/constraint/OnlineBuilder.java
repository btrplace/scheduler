/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;
import org.btrplace.model.Node;
import org.btrplace.model.constraint.Online;

import java.util.Collections;
import java.util.List;

/**
 * A builder for {@link Online} constraints.
 *
 * @author Fabien Hermenier
 */
public class OnlineBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public OnlineBuilder() {
        super("online", new ConstraintParam[]{new ListOfParam("$n", 1, BtrpOperand.Type.NODE, false)});
    }

    /**
     * Build an online constraint.
     *
     * @param args must be 1 scriptset of node. The set must not be empty
     * @return a constraint
     */
    @Override
    public List<Online> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
            @SuppressWarnings("unchecked")
            List<Node> ns = (List<Node>) params[0].transform(this, t, args.get(0));
            return ns != null ? Online.newOnline(ns) : Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
