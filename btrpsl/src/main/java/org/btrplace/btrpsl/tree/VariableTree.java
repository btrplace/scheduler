/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.SymbolsTable;
import org.btrplace.btrpsl.element.BtrpOperand;

/**
 * A Parser to get a variable.
 *
 * @author Fabien Hermenier
 */
public class VariableTree extends BtrPlaceTree {

  /**
   * The table of symbols to use.
   */
  private final SymbolsTable symbols;

    /**
     * Make a new parser.
     *
     * @param t    the root of the tree
     * @param errs the errors to report
     * @param syms the symbols' table to use
     */
    public VariableTree(Token t, ErrorReporter errs, SymbolsTable syms) {
        super(t, errs);
        symbols = syms;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        String lbl = token.getText();
        if (!symbols.isDeclared(lbl)) {
            return ignoreError("Unknown variable " + lbl);
        }
        return symbols.getSymbol(lbl);
    }
}
