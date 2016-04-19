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
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Ready;
import org.btrplace.model.constraint.SatConstraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A builder for {@link Ready} constraints.
 *
 * @author Fabien Hermenier
 */
public class ReadyBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public ReadyBuilder() {
        super("ready", new ConstraintParam[]{new ListOfParam("$vms", 1, BtrpOperand.Type.VM, false)});
    }

    /**
     * Build an online constraint.
     *
     * @param args must be 1 set of vms. The set must not be empty
     * @return a constraint
     */
    @Override
    public List<SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
            List<VM> s = (List<VM>) params[0].transform(this, t, args.get(0));
            if (s == null) {
                return Collections.emptyList();
            }
            List<SatConstraint> l = new ArrayList<>(s.size());
            for (VM v : s) {
                l.add(new Ready(v));
            }
            return l;
        }
        return Collections.emptyList();
    }
}
