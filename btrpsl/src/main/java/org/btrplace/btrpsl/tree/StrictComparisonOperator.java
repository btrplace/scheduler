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
 * A parser to check if the left operand is &gt; or &lt; to the right operand
 * Return 1 if equals, 0 otherwise. Types must be the same
 *
 * @author Fabien Hermenier
 */
public class StrictComparisonOperator extends BtrPlaceTree {

    private boolean reverse = false;

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param rev {@code true} for &gt;
     * @param errs the errors to report
     */
    public StrictComparisonOperator(Token t, boolean rev, ErrorReporter errs) {
        super(t, errs);
        this.reverse = rev;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        BtrpOperand l = getChild(0).go(this);
        BtrpOperand r = getChild(1).go(this);

        if (!reverse) {
            return l.gt(r);
        }
        return r.gt(l);

    }

}
