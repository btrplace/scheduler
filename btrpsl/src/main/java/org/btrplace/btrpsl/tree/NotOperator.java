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
 * A parser to sum two integers or to make the union between two sets with the same type
 *
 * @author Fabien Hermenier
 */
public class NotOperator extends BtrPlaceTree {

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param errs the errors to report
     */
    public NotOperator(Token t, ErrorReporter errs) {
        super(t, errs);
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        return getChild(0).go(this).not();
    }
}
