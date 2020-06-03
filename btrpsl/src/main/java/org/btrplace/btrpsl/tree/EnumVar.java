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
import org.btrplace.btrpsl.element.BtrpSet;
import org.btrplace.btrpsl.element.BtrpString;
import org.btrplace.btrpsl.element.IgnorableOperand;

/**
 * An enumeration of variables.
 *
 * @author Fabien Hermenier
 */
public class EnumVar extends BtrPlaceTree {

  private final SymbolsTable syms;

    /**
     * Make a new tree.
     *
     * @param payload the root token
     * @param sTable  to symbol table
     * @param errors  the errors to report
     */
    public EnumVar(Token payload, SymbolsTable sTable, ErrorReporter errors) {
        super(payload, errors);
        this.syms = sTable;
    }

    /**
     * Expand the enumeration.
     * Variables are not evaluated nor checked
     *
     * @return a set of string or an error
     */
    public BtrpOperand expand() {
        String head = getChild(0).getText().substring(0, getChild(0).getText().length() - 1);
        String tail = getChild(getChildCount() - 1).getText().substring(1);
        BtrpSet res = new BtrpSet(1, BtrpOperand.Type.STRING);

        for (int i = 1; i < getChildCount() - 1; i++) {
            BtrpOperand op = getChild(i).go(this);
            if (op == IgnorableOperand.getInstance()) {
                return op;
            }
            BtrpSet s = (BtrpSet) op;
            for (BtrpOperand o : s.getValues()) {
                //Compose
                res.getValues().add(new BtrpString(head + o.toString() + tail));
            }
        }
        return res;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        String head = getChild(0).getText().substring(0, getChild(0).getText().length() - 1);
        String tail = getChild(getChildCount() - 1).getText().substring(1);
        BtrpSet res = null;

        for (int i = 1; i < getChildCount() - 1; i++) {
            BtrpOperand op = getChild(i).go(this);
            if (op == IgnorableOperand.getInstance()) {
                return op;
            }
            BtrpSet s = (BtrpSet) op;
            for (BtrpOperand o : s.getValues()) {
                //Compose
                String id = head + o.toString() + tail;
                //lookup
                BtrpOperand var = syms.getSymbol(id);
                if (var == null) {
                    return ignoreError(parent.getToken(), "Unknown variable '" + id + "'");
                }
                if (res == null) {
                    res = new BtrpSet(2, var.type());
                }
                res.getValues().add(var);
            }
        }
        return res;
    }
}
