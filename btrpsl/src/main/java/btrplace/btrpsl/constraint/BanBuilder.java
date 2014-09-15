/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.btrpsl.constraint;

import btrplace.btrpsl.element.BtrpElement;
import btrplace.btrpsl.element.BtrpOperand;
import btrplace.btrpsl.tree.BtrPlaceTree;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.Ban;
import btrplace.model.constraint.SatConstraint;

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
        super("ban", new ConstraintParam[]{new ListOfParam("$v", 1, BtrpElement.Type.VM, false), new ListOfParam("$n", 1, BtrpOperand.Type.node, false)});
    }

    /**
     * Build a ban constraint.
     *
     *
     * @param t    the current tree
     * @param args must be 2 operands, first contains virtual machines and the second nodes. Each set must not be empty
     * @return a constraint
     */
    @Override
    public List<SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (!checkConformance(t, args)) {
            return Collections.emptyList();
        }
        List<VM> vms = (List<VM>) params[0].transform(this, t, args.get(0));
        List<Node> ns = (List<Node>) params[1].transform(this, t, args.get(1));
        if (vms != null && ns != null) {
            return (List) Ban.newBan(vms, ns);
        }
        return Collections.emptyList();
    }
}
