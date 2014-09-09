/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.btrpsl.tree;

import btrplace.btrpsl.*;
import btrplace.btrpsl.element.BtrpElement;
import btrplace.btrpsl.element.BtrpOperand;
import btrplace.btrpsl.element.BtrpSet;
import btrplace.btrpsl.element.IgnorableOperand;
import btrplace.btrpsl.template.ElementBuilderException;
import btrplace.btrpsl.template.TemplateFactory;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A statement to instantiate VMs or nodes from a specific template.
 *
 * @author Fabien Hermenier
 */
public class TemplateAssignment extends BtrPlaceTree {

    private NamingService namingService;

    private Model mo;

    /**
     * The current script.
     */
    private Script script;

    /**
     * The template factory.
     */
    private TemplateFactory tpls;

    private SymbolsTable syms;

    /**
     * Make a new tree.
     *
     * @param t            the token to consider
     * @param s            the script that is built
     * @param symbolsTable the symbol table
     * @param errs         the errors
     */
    public TemplateAssignment(Token t, Script s, TemplateFactory tplFactory, SymbolsTable symbolsTable, Model m, NamingService ns, ErrorReporter errs) {
        super(t, errs);
        this.script = s;
        this.tpls = tplFactory;
        this.syms = symbolsTable;
        this.mo = m;
        this.namingService = ns;
    }

    private Map<String, String> getTemplateOptions() {
        Map<String, String> opts = new HashMap<>();
        BaseTree t = getChild(1);
        for (int i = 0; i < t.getChildCount(); i++) {
            TemplateOptionTree opt = (TemplateOptionTree) t.getChild(i);
            opt.go(this);
            opts.put(opt.getKey(), opt.getValue());
        }
        return opts;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        BtrPlaceTree t = getChild(0);

        String tplName = getChild(1).getText();
        if (!tpls.isAvailable(tplName)) {
            return ignoreError("Unknown template '" + tplName + "'");
        }
        Map<String, String> opts = getTemplateOptions();

        int nType = t.getType();
        if (nType == ANTLRBtrplaceSL2Parser.IDENTIFIER) {
            addVM(tplName, script.id() + "." + t.getText(), opts);
        } else if (nType == ANTLRBtrplaceSL2Parser.NODE_NAME) {
            addNode(tplName, t.getText(), opts);
        } else if (nType == ANTLRBtrplaceSL2Parser.ENUM_ID) {
            BtrpOperand op = ((EnumElement) t).expand();

            if (op == IgnorableOperand.getInstance()) {
                return op;
            }

            for (BtrpOperand o : ((BtrpSet) op).getValues()) {
                addVM(tplName, o.toString(), opts);
            }
        } else if (nType == ANTLRBtrplaceSL2Parser.ENUM_FQDN) {
            BtrpOperand op = ((EnumElement) t).expand();

            if (op == IgnorableOperand.getInstance()) {
                return op;
            }

            for (BtrpOperand o : ((BtrpSet) op).getValues()) {
                addNode(tplName, o.toString(), opts);
            }
        } else if (nType == ANTLRBtrplaceSL2Parser.EXPLODED_SET) {
            List<BtrPlaceTree> children = t.getChildren();
            for (BtrPlaceTree child : children) {
                if (child.getType() == ANTLRBtrplaceSL2Parser.IDENTIFIER) {
                    addVM(tplName, script.id() + "." + child.getText(), opts);
                } else if (child.getType() == ANTLRBtrplaceSL2Parser.NODE_NAME) {
                    addNode(tplName, child.getText(), opts);
                } else {
                    return ignoreError("Only VMs or nodes can be declared from templates");
                }
            }
        } else {
            ignoreError("Unable to assign the template to '" + t.getText());
        }
        return IgnorableOperand.getInstance();
    }

    private void addVM(String tplName, String id, Map<String, String> opts) {

        try {
            BtrpElement el = namingService.resolve(id);
            if (el == null) {
                VM vm = mo.newVM();
                mo.getMapping().addReadyVM(vm);
                //We add the VM to the $me variable
                if (vm == null) {
                    ignoreError("No UUID to create node '" + id + "'");
                } else {
                    el = namingService.register(id, vm);
                    ((BtrpSet) syms.getSymbol(SymbolsTable.ME)).getValues().add(el);
                }
            }
            tpls.check(script, tplName, el.getElement(), opts);
            if (!script.add(el)) {
                ignoreError("VM '" + id + "' already created");
            }
        } catch (ElementBuilderException | NamingServiceException ex) {
            ignoreError(ex.getMessage());
        }
    }

    private void addNode(String tplName, String id, Map<String, String> opts) {
        try {
            BtrpElement el = namingService.resolve(id);
            if (el == null) {
                Node n = mo.newNode();
                mo.getMapping().addOfflineNode(n);
                if (n == null) {
                    ignoreError("No UUID to create node '" + id + "'");
                } else {
                    el = namingService.register(id, n);
                }
            }
            tpls.check(script, tplName, el.getElement(), opts);
            if (!script.add(el)) {
                ignoreError("Node '" + id + "' already created");
            }
        } catch (ElementBuilderException | NamingServiceException ex) {
            ignoreError(ex.getMessage());
        }
    }
}
