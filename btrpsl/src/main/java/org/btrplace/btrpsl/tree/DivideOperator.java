/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.IgnorableOperand;

/**
 * A parser to divide two integers or to split a set.
 *
 * @author Fabien Hermenier
 */
public class DivideOperator extends BtrPlaceTree {

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param errs the errors to report
     */
    public DivideOperator(Token t, ErrorReporter errs) {
        super(t, errs);
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        BtrpOperand l = getChild(0).go(this);
        BtrpOperand r = getChild(1).go(this);
        if (l != IgnorableOperand.getInstance() && r != IgnorableOperand.getInstance()) {
            try {
                return l.div(r);
            } catch (ArithmeticException e) {
                return ignoreError(e);
            }
        }
        return IgnorableOperand.getInstance();
    }
}
