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

import btrplace.btrpsl.element.BtrpOperand;
import btrplace.btrpsl.tree.BtrPlaceTree;
import btrplace.model.Node;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.SatConstraint;

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
        super("overbook", new ConstraintParam[]{new ListOfParam("$ns", 1, BtrpOperand.Type.node, false), new StringParam("$rcId"), new NumberParam("$r")});
    }

    @Override
    public List<SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (!checkConformance(t, args)) {
            return Collections.emptyList();
        }
        List<Node> s = (List<Node>) params[0].transform(this, t, args.get(0));
        String rcId = (String) params[1].transform(this, t, args.get(1));
        Number v = (Number) params[2].transform(this, t, args.get(2));
        if (v.doubleValue() < 0) {
            t.ignoreError("Parameter '" + params[1].getName() + "' expects a positive integer (" + v + " given)");
            return Collections.emptyList();
        }
        return (s != null && v != null && rcId != null ? (List) Overbook.newOverbooks(s, rcId, v.doubleValue()) : Collections.emptyList());
    }
}
