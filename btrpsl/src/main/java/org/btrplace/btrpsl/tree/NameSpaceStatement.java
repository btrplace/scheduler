/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.Script;
import org.btrplace.btrpsl.SymbolsTable;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.BtrpSet;
import org.btrplace.btrpsl.element.IgnorableOperand;

/**
 * A statement to specify the namespace of the script.
 *
 * @author Fabien Hermenier
 */
public class NameSpaceStatement extends BtrPlaceTree {

  private final SymbolsTable symbols;

  /**
   * The script that is built.
   */
  private final Script script;

    /**
     * Make a new statement.
     *
     * @param t    the token to consider
     * @param scr  the built script
     * @param syms the symbol table
     * @param errs the reported errors
     */
    public NameSpaceStatement(Token t, Script scr, SymbolsTable syms, ErrorReporter errs) {
        super(t, errs);
        this.script = scr;
        this.symbols = syms;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        StringBuilder fqdn = new StringBuilder();
        for (int i = 0; i < getChildCount(); i++) {
            fqdn.append(getChild(i));
            if (i != getChildCount() - 1) {
                fqdn.append('.');
            }
        }
        String id = fqdn.toString();
        script.setFullyQualifiedName(id);

        //$me is immutable and contains all the VMs.
        BtrpSet me = new BtrpSet(1, BtrpOperand.Type.VM);
        me.setLabel(SymbolsTable.ME);
        symbols.declareImmutable(me.label(), me);

        errors.updateNamespace();
        return IgnorableOperand.getInstance();
    }
}
