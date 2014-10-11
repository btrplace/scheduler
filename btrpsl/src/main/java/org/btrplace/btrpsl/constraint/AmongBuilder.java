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
        super("among", new ConstraintParam[]{new ListOfParam("$v", 1, BtrpOperand.Type.VM, false), new ListOfParam("$ns", 2, BtrpOperand.Type.node, false)});
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
    public List<SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
            List<VM> vms = (List<VM>) params[0].transform(this, t, args.get(0));
            Collection<Collection<Node>> nss = (Collection<Collection<Node>>) params[1].transform(this, t, args.get(1));
            return (vms != null && nss != null) ? (List) Collections.singletonList(new Among(vms, nss)) : Collections.emptyList();
        }
        return Collections.emptyList();
    }
}