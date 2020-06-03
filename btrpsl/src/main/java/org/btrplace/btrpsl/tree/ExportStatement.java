/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.Script;
import org.btrplace.btrpsl.antlr.ANTLRBtrplaceSL2Parser;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.BtrpSet;
import org.btrplace.btrpsl.element.IgnorableOperand;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Statement to specify a list of variables to export.
 * This is only allowed if the script belong to a specified namespace.
 * When a variable is exported, it is exported using its current identifier
 * and its fully qualified name, with is the variable identifier prefixed by the namespace.
 *
 * @author Fabien Hermenier
 */
public class ExportStatement extends BtrPlaceTree {

  private final Script script;

    /**
     * Make a new statement.
     *
     * @param t    the export token
     * @param scr  the script to alter with the variables to export
     * @param errs the list of errors
     */
    public ExportStatement(Token t, Script scr, ErrorReporter errs) {
        super(t, errs);
        this.script = scr;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {

        Set<String> scope = new HashSet<>();
        List<BtrpOperand> toAdd = new ArrayList<>();

        for (int i = 0; i < getChildCount(); i++) {
            if (getChild(i).getType() == ANTLRBtrplaceSL2Parser.ENUM_VAR) {
                BtrpOperand r = getChild(i).go(this);
                if (r == IgnorableOperand.getInstance()) {
                    return r;
                }
                for (BtrpOperand o : ((BtrpSet) r).getValues()) {
                    toAdd.add(o);
                }
            } else if (getChild(i).getType() == ANTLRBtrplaceSL2Parser.VARIABLE) {
                toAdd.add(getChild(i).go(this));
            } else if (getChild(i).getType() == ANTLRBtrplaceSL2Parser.TIMES) {
                scope.add("*");
            } else if (getChild(i).getType() == ANTLRBtrplaceSL2Parser.IDENTIFIER) {
                scope.add(getChild(i).getText());
            } else {
                BtrpOperand e = getChild(i).go(this);
                if (e == IgnorableOperand.getInstance()) {
                    return e;
                }
                toAdd.addAll(flatten(e));
            }
        }
        try {
            for (BtrpOperand op : toAdd) {
                script.addExportable(op.label(), op, scope);
            }
        } catch (UnsupportedOperationException ex) {
            return ignoreError(ex);
        }


        return IgnorableOperand.getInstance();
    }

    private static List<BtrpOperand> flatten(BtrpOperand o) {
        List<BtrpOperand> ret = new ArrayList<>();
        if (o.label() != null) {
            ret.add(o);
        } else {
            if (o.degree() == 0) {
                throw new UnsupportedOperationException(o + ": Variable expected");
            }
            BtrpSet s = (BtrpSet) o;
            for (BtrpOperand so : s.getValues()) {
                ret.addAll(flatten(so));
            }
        }
        return ret;
    }
}
