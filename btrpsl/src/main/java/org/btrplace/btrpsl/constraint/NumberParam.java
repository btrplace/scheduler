/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.element.BtrpNumber;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.IgnorableOperand;
import org.btrplace.btrpsl.tree.BtrPlaceTree;

/**
 * A parameter for a constraint that denotes a number.
 *
 * @author Fabien Hermenier
 */
public class NumberParam extends DefaultConstraintParam<Number> {

    /**
     * Make a new number parameter.
     *
     * @param n the parameter value
     */
    public NumberParam(String n) {
        super(n, "number");
    }

    @Override
    public Number transform(SatConstraintBuilder cb, BtrPlaceTree tree, BtrpOperand op) {
        if (op == IgnorableOperand.getInstance()) {
            throw new UnsupportedOperationException();
        }
        BtrpNumber n = (BtrpNumber) op;
        if (n.isInteger()) {
            return n.getIntValue();
        }
        return n.getDoubleValue();

    }

    @Override
    public boolean isCompatibleWith(BtrPlaceTree t, BtrpOperand o) {
        return o == IgnorableOperand.getInstance() || (o.type() == BtrpOperand.Type.NUMBER && o.degree() == 0);
    }
}
