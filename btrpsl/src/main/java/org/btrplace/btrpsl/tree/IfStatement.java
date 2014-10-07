/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

    private SymbolsTable sTable;

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
