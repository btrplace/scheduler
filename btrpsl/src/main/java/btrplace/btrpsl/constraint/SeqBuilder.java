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

package btrplace.btrpsl.constraint;

import btrplace.btrpsl.element.BtrpOperand;
import btrplace.btrpsl.tree.BtrPlaceTree;
import btrplace.model.VM;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.Seq;

import java.util.Collections;
import java.util.List;

/**
 * A builder for {@link btrplace.model.constraint.Seq} constraints.
 *
 * @author Fabien Hermenier
 */
public class SeqBuilder extends DefaultSatConstraintBuilder {

    /**
     * Make a new builder.
     */
    public SeqBuilder() {
        super("seq", new ConstraintParam[]{new ListOfParam("$v", 1, BtrpOperand.Type.VM, false)});
    }

    @Override
    public List<SatConstraint> buildConstraint(BtrPlaceTree t, List<BtrpOperand> args) {
        if (checkConformance(t, args)) {
            List<VM> vms = (List<VM>) params[0].transform(this, t, args.get(0));
            return (vms != null ? (List) Collections.singletonList(new Seq(vms)) : Collections.emptyList());
        }
        return Collections.emptyList();
    }
}
