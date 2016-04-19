/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
        super("splitAmong", new ConstraintParam[]{new ListOfParam("$vms", 2, BtrpOperand.Type.VM, false), new ListOfParam("$ns", 2, BtrpOperand.Type.node, false)});
    }

    /**
     * Build a constraint.
     *
     * @param args the parameters of the constraint. Must be 2 non-empty set of virtual machines.
     * @return the constraint
     */
    @Override
    public List<SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
            Collection<Collection<VM>> vs = (Collection<Collection<VM>>) params[0].transform(this, t, args.get(0));
            Collection<Collection<Node>> ps = (Collection<Collection<Node>>) params[1].transform(this, t, args.get(1));
            return (vs != null && ps != null ? (List) Collections.singletonList(new SplitAmong(vs, ps, false)) : Collections.emptyList());
        }
        return Collections.emptyList();
    }
}
