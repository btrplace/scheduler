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
import org.btrplace.model.constraint.Fence;

import java.util.Collections;
import java.util.List;

/**
 * A builder to for {@link Fence} constraints.
 *
 * @author Fabien Hermenier
 */
public class FenceBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public FenceBuilder() {
        super("fence", new ConstraintParam[]{new ListOfParam("$v", 1, BtrpOperand.Type.VM, false), new ListOfParam("$n", 1, BtrpOperand.Type.NODE, false)});
    }

    /**
     * Build a constraint.
     *
     * @param args the parameters to use. Must be 2 non-empty set. One of virtual machines and one of nodes.
     * @return a constraint
     */
    @Override
    public List<Fence> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
          @SuppressWarnings("unchecked")
          List<VM> vms = (List<VM>) params[0].transform(this, t, args.get(0));
          @SuppressWarnings("unchecked")
          List<Node> ns = (List<Node>) params[1].transform(this, t, args.get(1));
            return vms != null && ns != null ? Fence.newFence(vms, ns) : Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
