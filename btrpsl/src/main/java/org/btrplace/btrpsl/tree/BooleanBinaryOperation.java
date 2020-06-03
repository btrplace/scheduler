/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.element.BtrpNumber;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.IgnorableOperand;

/**
 * A tree for binary boolean expressions. Accept either 'and' or 'or' operations.
 *
 * @author Fabien Hermenier
 */
public class BooleanBinaryOperation extends BtrPlaceTree {

  private final boolean and;

    /**
     * Make a new operator
     *
     * @param t    the 'OR' token
     * @param a    {@code true} for a boolean 'and' operation. {@code false} for a 'or'.
     * @param errs the list of errors.
     */
    public BooleanBinaryOperation(Token t, boolean a, ErrorReporter errs) {
        super(t, errs);
        this.and = a;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        BtrpOperand l = getChild(0).go(this);
        BtrpOperand r = getChild(1).go(this);

        if (l == IgnorableOperand.getInstance() || r == IgnorableOperand.getInstance()) {
            return IgnorableOperand.getInstance();
        }
        if (!(l instanceof BtrpNumber)) {
            return ignoreError("Expression expected, but was '" + l + "'");
        }
        if (!(r instanceof BtrpNumber)) {
            return ignoreError("Expression expected, but was '" + r + "'");
        }

        boolean b1 = BtrpNumber.TRUE.equals(l);
        boolean b2 = BtrpNumber.TRUE.equals(r);

        return eval(b1, b2);
    }

    private BtrpNumber eval(boolean b1, boolean b2) {
        if (and) {
            return b1 && b2 ? BtrpNumber.TRUE : BtrpNumber.FALSE;
        }
        return b1 || b2 ? BtrpNumber.TRUE : BtrpNumber.FALSE;
    }
}
