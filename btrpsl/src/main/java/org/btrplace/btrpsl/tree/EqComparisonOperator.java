/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.element.BtrpOperand;

/**
 * A parser to check the equality between two operands.
 * Return 1 if equals, 0 otherwise. Types must be the same
 *
 * @author Fabien Hermenier
 */
public class EqComparisonOperator extends BtrPlaceTree {

    private boolean opposite = false;

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param opp {@code true} for non-equality check.
     * @param errs the errors to report
     */
    public EqComparisonOperator(Token t, boolean opp, ErrorReporter errs) {
        super(t, errs);
        this.opposite = opp;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        BtrpOperand l = getChild(0).go(this);
        BtrpOperand r = getChild(1).go(this);
        if (!opposite) {
            return l.eq(r);
        }
        return l.eq(r).not();
    }

}
