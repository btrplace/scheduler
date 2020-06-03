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
import org.btrplace.btrpsl.element.BtrpSet;

/**
 * A parser get the cardinality of a set.
 *
 * @author Fabien Hermenier
 */
public class CardinalityOperator extends BtrPlaceTree {

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param errs the errors to report
     */
    public CardinalityOperator(Token t, ErrorReporter errs) {
        super(t, errs);
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        BtrpOperand c = getChild(0).go(this);
        if (c.degree() == 0) {
            return ignoreError("Cardinality operator only applies to a set");
        }
        return new BtrpNumber(((BtrpSet) c).size(), BtrpNumber.Base.BASE_10);
    }
}
