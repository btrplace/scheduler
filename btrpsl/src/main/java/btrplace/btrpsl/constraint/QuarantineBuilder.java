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
import btrplace.model.constraint.Quarantine;
import btrplace.model.constraint.SatConstraint;

import java.util.Collections;
import java.util.List;

/**
 * a builder for {@link Quarantine} constraints.
 *
 * @author Fabien Hermenier
 */
public class QuarantineBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public QuarantineBuilder() {
        super("quarantine", new ConstraintParam[]{new ListOfParam("$n", 1, BtrpOperand.Type.node, false)});
    }

    @Override
    public List<SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
            List<Node> ns = (List<Node>) params[0].transform(this, t, args.get(0));
            return (ns != null ? (List) Quarantine.newQuarantine(ns) : Collections.emptyList());
        }
        return Collections.emptyList();
    }
}
