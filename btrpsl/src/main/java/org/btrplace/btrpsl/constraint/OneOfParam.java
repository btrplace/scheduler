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
import org.btrplace.btrpsl.element.IgnorableOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A parameter for a constraint that can have multiple arguments types.
 *
 * @author Vincent Kherbache
 */
public class OneOfParam extends DefaultConstraintParam<Object> {

    protected List<ConstraintParam> paramsList;

    /**
     * Make a new multiple types parameter.
     *
     * @param n         the parameter name
     * @param params    the list of different parameters types
     */
    public OneOfParam(String n, ConstraintParam... params) {
        super(n, "oneOf");
        paramsList = Arrays.asList(params);
    }

    @Override
    public String prettySignature() {

        StringBuilder b = new StringBuilder();
        for (Iterator<ConstraintParam> ite = paramsList.iterator(); ite.hasNext(); ) {
            b.append(ite.next().prettySignature());
            if (ite.hasNext()) {
                b.append(" || ");
            }
        }
        return b.toString();
    }

    @Override
    public String fullSignature() {
        return getName() + ": " + prettySignature();
    }

    @Override
    public Object transform(SatConstraintBuilder cb, BtrPlaceTree tree, BtrpOperand op) {

        if (op == IgnorableOperand.getInstance()) {
            return null;
        }

        for (ConstraintParam c : paramsList) {
            if (c.isCompatibleWith(tree, op)) {
                return c.transform(cb, tree, op);
            }
        }
        tree.ignoreError("Unsupported type '" + op.type() + "'");
        return null;
    }

    @Override
    public boolean isCompatibleWith(BtrPlaceTree t, BtrpOperand o) {
        if (o == IgnorableOperand.getInstance()) {
            return true;
        }
        for (ConstraintParam c : paramsList) {
            if (c.isCompatibleWith(t, o)) {
                return true;
            }
        }
        return false;
    }
}
