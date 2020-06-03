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
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.SplitAmong;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A builder for {@link SplitAmong} constraints.
 *
 * @author Fabien Hermenier
 */
public class SplitAmongBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public SplitAmongBuilder() {
        super("splitAmong", new ConstraintParam[]{new ListOfParam("$vms", 2, BtrpOperand.Type.VM, false), new ListOfParam("$ns", 2, BtrpOperand.Type.NODE, false)});
    }

    /**
     * Build a constraint.
     *
     * @param args the parameters of the constraint. Must be 2 non-empty set of virtual machines.
     * @return the constraint
     */
    @Override
    public List<? extends SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
            @SuppressWarnings("unchecked")
            Collection<Collection<VM>> vs = (Collection<Collection<VM>>) params[0].transform(this, t, args.get(0));
            @SuppressWarnings("unchecked")
            Collection<Collection<Node>> ps = (Collection<Collection<Node>>) params[1].transform(this, t, args.get(1));
            return vs != null && ps != null ? Collections.singletonList(new SplitAmong(vs, ps, false)) : Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
