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
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.BtrpSet;
import org.btrplace.btrpsl.element.IgnorableOperand;

/**
 * An iterator statement. The loop iterate of the element of the set given in parameter.
 *
 * @author Fabien Hermenier
 */
public class ForStatement extends BtrPlaceTree {

    private SymbolsTable table;

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param syms the symbol table
     * @param errs the errors to report
     */
    public ForStatement(Token t, SymbolsTable syms, ErrorReporter errs) {
        super(t, errs);
        this.table = syms;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {

        //First child is variable, second is set we iterate over, third is a block
        if (this.getChildCount() != 3) {
            return ignoreError("Malformed iteration loop");
        }

        table.pushTable();
        String inVar = getChild(0).getText();
        if (table.isDeclared(inVar)) {
            return ignoreError("Variable " + inVar + " already declared");
        }

        BtrpOperand c = getChild(1).go(this);
        if (c == IgnorableOperand.getInstance()) {
            return c;
        }
        if (c.degree() < 1) {
            return ignoreError("The literal to iterate one must be a set");
        }
        BtrpSet set = (BtrpSet) c;
        for (Object elem : set.getValues()) {
            table.declare(inVar, (BtrpOperand) elem);
            getChild(2).go(this);
            //TODO a good solution to avoid to iterate once an iteration fail?
        }

        if (!table.popTable()) {
            return ignoreError("Unable to Pop the symbol table");
        }
        return IgnorableOperand.getInstance();
    }

}
