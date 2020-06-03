/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.antlr.ANTLRBtrplaceSL2Lexer;
import org.btrplace.btrpsl.element.BtrpNumber;
import org.btrplace.btrpsl.element.BtrpOperand;

/**
 * A parser to make integer.
 *
 * @author Fabien Hermenier
 */
public class NumberTree extends BtrPlaceTree {

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param errs the errors to report
     */
    public NumberTree(Token t, ErrorReporter errs) {
        super(t, errs);
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {

        switch (token.getType()) {
            case ANTLRBtrplaceSL2Lexer.OCTAL:
                return new BtrpNumber(Integer.parseInt(getText().substring(1), 8), BtrpNumber.Base.BASE_8);
            case ANTLRBtrplaceSL2Lexer.HEXA:
                return new BtrpNumber(Integer.parseInt(getText().substring(2), 16), BtrpNumber.Base.BASE_16);
            case ANTLRBtrplaceSL2Lexer.DECIMAL:
                return new BtrpNumber(Integer.parseInt(getText()), BtrpNumber.Base.BASE_10);
            case ANTLRBtrplaceSL2Lexer.FLOAT:
                return new BtrpNumber(Double.parseDouble(getText()));
            default:
                return ignoreError("Unsupported integer format: " + getText());
        }
    }
}
