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
import org.btrplace.model.constraint.Among;
import org.btrplace.model.constraint.SatConstraint;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Builder for {@link Among} constraints.
 *
 * @author Fabien Hermenier
 */
public class AmongBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public AmongBuilder() {
        super("among", new ConstraintParam[]{new ListOfParam("$v", 1, BtrpOperand.Type.VM, false), new ListOfParam("$ns", 2, BtrpOperand.Type.NODE, false)});
    }

    /**
     * Build a constraint.
     *
     * @param t    the current tree
     * @param args the argument. Must be a non-empty set of virtual machines and a multiset of nodes with
     *             at least two non-empty sets. If the multi set contains only one set, a {@code Fence} constraint is created
     * @return the constraint
     */
    @Override
    public List<? extends SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
          @SuppressWarnings("unchecked")
          List<VM> vms = (List<VM>) params[0].transform(this, t, args.get(0));
          @SuppressWarnings("unchecked")
          Collection<Collection<Node>> nss = (Collection<Collection<Node>>) params[1].transform(this, t, args.get(1));
            return vms != null && nss != null ? Collections.singletonList(new Among(vms, nss)) : Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
