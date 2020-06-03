/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.SymbolsTable;
import org.btrplace.btrpsl.antlr.ANTLRBtrplaceSL2Parser;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.IgnorableOperand;

/**
 * A parser to declare a variable.
 *
 * @author Fabien Hermenier
 */
public class SelfAssignmentStatement extends BtrPlaceTree {

    /**
     * The operation.
     */
    public enum Type {
        /**
         * +=
         */PLUS_EQUALS,
        /**
         * -=
         */MINUS_EQUALS,
        /**
         * /=
         */DIV_EQUALS,
        /**
         * %=
         */REMAINDER_EQUALS,
        /**
         * =
         */TIMES_EQUALS
    }

  private final Type type;

  /**
   * The table of symbols to use;
   */
  private final SymbolsTable symbols;

    /**
     * Make a new parser
     *
     * @param t    the type
     * @param tok the analysed token
     * @param errs the errors to report
     * @param syms the symbol table
     */
    public SelfAssignmentStatement(Type t, Token tok, ErrorReporter errs, SymbolsTable syms) {
        super(tok, errs);
        this.type = t;
        this.symbols = syms;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {

        if (getChild(0).getType() == ANTLRBtrplaceSL2Parser.VARIABLE) {
            String lbl = getChild(0).getText();
            if (symbols.isImmutable(lbl)) {
                return ignoreError(lbl + " is an immutable variable. Assignment not permitted");
            }
            BtrpOperand e = getChild(0).go(parent);
            BtrpOperand r = getChild(1).go(this);
            BtrpOperand res;
            try {
            switch (type) {
                case PLUS_EQUALS:
                    res = e.plus(r);
                    break;
                case MINUS_EQUALS:
                    res = e.minus(r);
                    break;
                case TIMES_EQUALS:
                    res = e.times(r);
                    break;
                case DIV_EQUALS:
                    res = e.div(r);
                    break;
                case REMAINDER_EQUALS:
                    res = e.remainder(r);
                    break;
                default:
                    return ignoreError("Unsupported operation: " + type);
            }
            } catch (ArithmeticException ex) {
                return ignoreError(ex);
            }
            symbols.declare(lbl, res);
            res.setLabel(lbl);
            return IgnorableOperand.getInstance();
        }
        return ignoreError(getChild(0).getText() + " is not a variable");
    }
}
