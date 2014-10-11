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
import org.btrplace.btrpsl.ANTLRBtrplaceSL2Parser;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.SymbolsTable;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.BtrpSet;
import org.btrplace.btrpsl.element.IgnorableOperand;

import java.util.List;

/**
 * A parser to declare a variable.
 *
 * @author Fabien Hermenier
 */
public class AssignmentStatement extends BtrPlaceTree {

    /**
     * The table of symbols to use;
     */
    private final SymbolsTable symbols;

    /**
     * Make a new parser
     *
     * @param t    the root token
     * @param errs the errors to report
     * @param syms the table of symbols to use
     */
    public AssignmentStatement(Token t, ErrorReporter errs, SymbolsTable syms) {
        super(t, errs);
        symbols = syms;
    }

    private BtrpOperand declareVariable(String lbl, BtrpOperand res) {
        if (symbols.isImmutable(lbl)) {
            return ignoreError(lbl + " is an immutable variable. Assignment not permitted");
        }
        BtrpOperand cpy = res.clone();
        cpy.setLabel(lbl);
        symbols.declare(lbl, cpy);
        return IgnorableOperand.getInstance();
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        //We evaluate right operand
        try {
            BtrpOperand res = getChild(1).go(this);
            if (res == IgnorableOperand.getInstance()) {
                //We declare the variable to reduce the number of errors
                symbols.declare(getChild(0).getText(), res);
                return res;
            }
            if (getChild(0).getType() == ANTLRBtrplaceSL2Parser.VARIABLE) {
                return declareVariable(getChild(0).getText(), res);
            } else if (getChild(0).getType() == ANTLRBtrplaceSL2Parser.EXPLODED_SET) {
                List<BtrpOperand> vals = ((BtrpSet) res).getValues();
                BtrPlaceTree t = getChild(0);
                for (int i = 0; i < t.getChildCount(); i++) {
                    switch (t.getChild(i).getType()) {
                        case ANTLRBtrplaceSL2Parser.VARIABLE:
                            if (i < vals.size()) {
                                declareVariable(t.getChild(i).getText(), vals.get(i));
                            }
                            break;
                        case ANTLRBtrplaceSL2Parser.BLANK:
                            break;
                        default:
                            return ignoreError("Unsupported type for decomposition: " + t);
                    }
                }
            } else if (getChild(0).getType() == ANTLRBtrplaceSL2Parser.ENUM_VAR) {
                List<BtrpOperand> vals = ((BtrpSet) res).getValues();
                BtrpOperand op = ((EnumVar) getChild(0)).expand();
                if (op == IgnorableOperand.getInstance()) {
                    return op;
                }
                BtrpSet vars = (BtrpSet) op;
                for (int i = 0; i < vars.getValues().size(); i++) {
                    BtrpOperand o = vars.getValues().get(i);
                    if (i < vals.size()) {
                        declareVariable(o.toString(), vals.get(i));
                    } else {
                        break;
                    }
                }
            } else {
                return ignoreError("Unsupported decomposition");
            }
        } catch (UnsupportedOperationException e) {
            return ignoreError(e.getMessage());
        }
        return IgnorableOperand.getInstance();
    }
}
