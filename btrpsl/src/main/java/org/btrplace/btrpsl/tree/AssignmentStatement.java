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
        BtrpOperand cpy = res.copy();
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
            return ignoreError(e);
        }
        return IgnorableOperand.getInstance();
    }
}
