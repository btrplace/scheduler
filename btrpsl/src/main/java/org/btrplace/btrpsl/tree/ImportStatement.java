/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
  private final Includes includes;

    /**
     * The symbol table to fulfil.
     */
    private final SymbolsTable symTable;

  private final Script script;

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

    private String scriptId() {
        StringBuilder scriptId = new StringBuilder();
        for (int i = 0; i < getChildCount(); i++) {
            scriptId.append(getChild(i));
            if (i != getChildCount() - 1) {
                scriptId.append('.');
            }
        }
        return scriptId.toString();
    }

    @Override
    @SuppressWarnings("squid:S1166")
    public BtrpOperand go(BtrPlaceTree parent) {
        String id = scriptId();
        List<Script> res;
        try {
            res = includes.getScripts(id);
            script.getDependencies().addAll(res);
        } catch (ScriptBuilderException e) {
            int nb = e.getErrorReporter().getErrors().size();
            return ignoreError(nb + " error(s) imported through '" + id + "'");
        }
        if (res.isEmpty()) {
            return ignoreError(getChild(0).getToken(), "Unable to locate '" + id + "'");
        }

        if (id.endsWith(".*")) { //Prepare the global variable.
            BtrpSet global = new BtrpSet(1, BtrpOperand.Type.VM);
            global.setLabel("$".concat(id.substring(0, id.length() - 2)));

            if (global.size() > 0 && !symTable.declareImmutable(global.label(), global)) {
                return ignoreError("Unable to add variable '" + global.label() + "'");
            }
        }
        for (Script v : res) {
            for (BtrpOperand op : v.getImportables(script.id())) {
                String fqn = v.fullyQualifiedSymbolName(op.label());
                if (!symTable.declareImmutable(fqn, op)) {
                    return ignoreError("Unable to import '" + fqn + "': already declared");
                }
            }
        }
        return IgnorableOperand.getInstance();
    }
}
