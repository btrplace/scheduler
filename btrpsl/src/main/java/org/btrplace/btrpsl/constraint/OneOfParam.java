/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

    protected List<ConstraintParam<?>> paramsList;

    /**
     * Make a new multiple types parameter.
     *
     * @param n         the parameter name
     * @param params    the list of different parameters types
     */
    public OneOfParam(String n, ConstraintParam<?>... params) {
        super(n, "oneOf");
        paramsList = Arrays.asList(params);
    }

    @Override
    public String prettySignature() {

        StringBuilder b = new StringBuilder();
        for (Iterator<ConstraintParam<?>> ite = paramsList.iterator(); ite.hasNext(); ) {
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

        for (ConstraintParam<?> c : paramsList) {
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
        for (ConstraintParam<?> c : paramsList) {
            if (c.isCompatibleWith(t, o)) {
                return true;
            }
        }
        return false;
    }
}
