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

    private SymbolsTable symbols;

    /**
     * The script that is built.
     */
    private Script script;

    /**
     * Make a new statement.
     *
     * @param t    the token to consider
     * @param scr  the built script
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
