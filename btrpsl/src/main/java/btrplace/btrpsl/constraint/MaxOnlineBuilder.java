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
import btrplace.model.constraint.MaxOnline;
import btrplace.model.constraint.SatConstraint;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A builder for {@link btrplace.model.constraint.MaxOnline} constraints.
 *
 * @author Fabien Hermenier
 */
public class MaxOnlineBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public MaxOnlineBuilder() {
        super("maxOnline", new ConstraintParam[]{new ListOfParam("$n", 1, BtrpOperand.Type.node, false), new NumberParam("$nb")});
    }

    /**
     * Build an online constraint.
     *
     * @param args must be 1 set of nodes. The set must not be empty
     * @return a constraint
     */
    @Override
    public List<SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
            List<Node> ns = (List<Node>) params[0].transform(this, t, args.get(0));
            Number v = (Number) params[1].transform(this, t, args.get(1));
            if (v.doubleValue() < 0) {
                t.ignoreError("Parameter '" + params[1].getName() + "' expects a positive integer (" + v + " given)");
                return Collections.emptyList();
            }

            if (v != null && Math.rint(v.doubleValue()) != v.doubleValue()) {
                t.ignoreError("Parameter '" + params[1].getName() + "' expects an integer, not a real number (" + v + " given)");
                return Collections.emptyList();
            }

            if (ns == null || v == null) {
                return Collections.emptyList();
            }
            Set<Node> s = new HashSet<>(ns);
            if (s.size() != ns.size()) {                //Prevent duplicates
                return Collections.emptyList();
            }

            return (List) Collections.singletonList(new MaxOnline(new HashSet<>(ns), v.intValue(), true));
        }
        return Collections.emptyList();
    }
}
