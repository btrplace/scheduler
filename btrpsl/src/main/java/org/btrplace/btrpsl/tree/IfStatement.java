/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.SymbolsTable;
import org.btrplace.btrpsl.element.BtrpNumber;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.IgnorableOperand;

/**
 * A parser to check if the left child expression is true. In this
 * situation, it executes the child of the left expression. Otherwise,
 * it executes the direct right child.
 *
 * @author Fabien Hermenier
 */
public class IfStatement extends BtrPlaceTree {

  private final SymbolsTable sTable;

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param syms the symbol table to use
     * @param errs the errors to report
     */
    public IfStatement(Token t, SymbolsTable syms, ErrorReporter errs) {
        super(t, errs);
        this.sTable = syms;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        BtrpOperand expr = getChild(0).go(this);
        if (expr.equals(BtrpNumber.TRUE)) {
            sTable.pushTable();
            getChild(1).go(this);
            sTable.popTable();
        } else if (getChildCount() == 3) {
            sTable.pushTable();
            getChild(2).go(this);
            sTable.popTable();

        } else {
            return ignoreError(expr + ": not an expression");
        }
        return IgnorableOperand.getInstance();
    }

}
