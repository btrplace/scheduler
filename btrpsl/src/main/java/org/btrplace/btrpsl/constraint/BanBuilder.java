/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Ban;

import java.util.Collections;
import java.util.List;

/**
 * Builder for {@link Ban} constraints.
 *
 * @author Fabien Hermenier
 */
public class BanBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public BanBuilder() {
        super("ban", new ConstraintParam[]{new ListOfParam("$v", 1, BtrpOperand.Type.VM, false), new ListOfParam("$n", 1, BtrpOperand.Type.NODE, false)});
    }

    /**
     * Build a ban constraint.
     *
     * @param t    the current tree
     * @param args must be 2 operands, first contains virtual machines and the second nodes. Each set must not be empty
     * @return a constraint
     */
    @Override
    public List<Ban> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (!checkConformance(t, args)) {
            return Collections.emptyList();
        }
      @SuppressWarnings("unchecked")
      List<VM> vms = (List<VM>) params[0].transform(this, t, args.get(0));
      @SuppressWarnings("unchecked")
      List<Node> ns = (List<Node>) params[1].transform(this, t, args.get(1));
        if (vms != null && ns != null) {
            return Ban.newBan(vms, ns);
        }
        return Collections.emptyList();
    }
}
