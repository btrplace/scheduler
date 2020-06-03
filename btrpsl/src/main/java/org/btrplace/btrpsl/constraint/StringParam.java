/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.IgnorableOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;

/**
 * A parameter for a constraint that denotes a String.
 *
 * @author Fabien Hermenier
 */
public class StringParam extends DefaultConstraintParam<String> {

    /**
     * Make a new string parameter.
     *
     * @param n the parameter value
     */
    public StringParam(String n) {
        super(n, "string");
    }

    @Override
    public String transform(SatConstraintBuilder cb, BtrPlaceTree tree, BtrpOperand op) {
        if (op == IgnorableOperand.getInstance()) {
            throw new UnsupportedOperationException();
        }
        return op.toString();
    }

    @Override
    public boolean isCompatibleWith(BtrPlaceTree t, BtrpOperand o) {
        return o == IgnorableOperand.getInstance() || (o.type() == BtrpOperand.Type.STRING && o.degree() == 0);
    }
}
