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
import org.btrplace.btrpsl.ScriptBuilderException;
import org.btrplace.btrpsl.SymbolsTable;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.BtrpSet;
import org.btrplace.btrpsl.element.IgnorableOperand;
import org.btrplace.btrpsl.includes.Includes;

import java.util.List;

/**
 * Statement to import some other script wrt. their namespace.
 * If a valid namespace is found. Then, the current symbol table will be
 * completed with the exported variables. In case of conflicts, conflicting
 * variables are removed. This should only occurs with short variables. Fully Qualified
 * variable names should not be affected.
 *
 * @author Fabien Hermenier
 */
public class ImportStatement extends BtrPlaceTree {

    /**
     * The list of available includes.
     */
    private Includes includes;

    /**
     * The symbol table to fulfil.
     */
    private SymbolsTable symTable;

    private Script script;

    /**
     * Make a new statement
     *
     * @param t      the 'IMPORT' token
     * @param incs   the list of available includes
     * @param sTable the symbol table.
     * @param scr    the currently built script
     * @param errs   the list of errors.
     */
    public ImportStatement(Token t, Includes incs, SymbolsTable sTable, Script scr, ErrorReporter errs) {
        super(t, errs);
        this.includes = incs;
        this.symTable = sTable;
        this.script = scr;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        StringBuilder scriptId = new StringBuilder();
        for (int i = 0; i < getChildCount(); i++) {
            scriptId.append(getChild(i));
            if (i != getChildCount() - 1) {
                scriptId.append('.');
            }
        }
        String id = scriptId.toString();
        List<Script> res;
        try {
            res = includes.getScripts(id);
            script.getDependencies().addAll(res);
        } catch (ScriptBuilderException e) {
            int nb = e.getErrorReporter().getErrors().size();
            return ignoreError(Integer.toString(nb) + " error(s) imported through '" + id + "'");
        }
        if (res.isEmpty()) {
            return ignoreError(getChild(0).getToken(), "Unable to locate '" + id + "'");
        }

        BtrpSet global = null;
        if (id.endsWith(".*")) { //Prepare the global variable.
            global = new BtrpSet(1, BtrpOperand.Type.VM);
            global.setLabel("$".concat(id.substring(0, id.length() - 2)));
        }
        for (Script v : res) {
            List<BtrpOperand> toImport = v.getImportables(script.id());
            for (BtrpOperand op : toImport) {
                String fqn = v.fullyQualifiedSymbolName(op.label());
                if (!symTable.declareImmutable(fqn, op)) {
                    return ignoreError("Unable to import '" + fqn + "': already declared");
                }
            }
        }
        if (global != null && global.size() > 0 && !symTable.declareImmutable(global.label(), global)) {
            return ignoreError("Unable to add variable '" + global.label() + "'");
        }
        return IgnorableOperand.getInstance();
    }
}
