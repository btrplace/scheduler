/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;
import org.btrplace.model.Node;
import org.btrplace.model.constraint.Overbook;

import java.util.Collections;
import java.util.List;

/**
 * A builder for {@link Overbook} constraints.
 *
 * @author Fabien Hermenier
 */
public class OverbookBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public OverbookBuilder() {
        super("overbook", new ConstraintParam[]{new ListOfParam("$ns", 1, BtrpOperand.Type.NODE, false), new StringParam("$rcId"), new NumberParam("$r")});
    }

    @Override
    public List<Overbook> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (!checkConformance(t, args)) {
            return Collections.emptyList();
        }
        @SuppressWarnings("unchecked")
        List<Node> s = (List<Node>) params[0].transform(this, t, args.get(0));
        String rcId = (String) params[1].transform(this, t, args.get(1));
        Number v = (Number) params[2].transform(this, t, args.get(2));
        if (v == null || v.doubleValue() < 0) {
            t.ignoreError("Parameter '" + params[1].getName() + "' expects a positive integer (" + v + " given)");
            return Collections.emptyList();
        }
        return s != null && rcId != null ? Overbook.newOverbooks(s, rcId, v.doubleValue()) : Collections.emptyList();
    }
}
