/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;
import org.btrplace.model.Node;
import org.btrplace.model.constraint.Quarantine;
import org.btrplace.model.constraint.SatConstraint;

import java.util.Collections;
import java.util.List;

/**
 * a builder for {@link Quarantine} constraints.
 *
 * @author Fabien Hermenier
 */
public class QuarantineBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public QuarantineBuilder() {
        super("quarantine", new ConstraintParam[]{new ListOfParam("$n", 1, BtrpOperand.Type.NODE, false)});
    }

    @Override
    public List<? extends SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
            @SuppressWarnings("unchecked")
            List<Node> ns = (List<Node>) params[0].transform(this, t, args.get(0));
            return ns != null ? Quarantine.newQuarantine(ns) : Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
