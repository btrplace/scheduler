/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.Script;
import org.btrplace.btrpsl.SymbolsTable;
import org.btrplace.btrpsl.antlr.ANTLRBtrplaceSL2Parser;
import org.btrplace.btrpsl.element.BtrpElement;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.BtrpSet;
import org.btrplace.btrpsl.element.IgnorableOperand;
import org.btrplace.btrpsl.template.ElementBuilderException;
import org.btrplace.btrpsl.template.TemplateFactory;
import org.btrplace.model.Element;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.NamingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A statement to instantiate VMs or nodes from a specific template.
 *
 * @author Fabien Hermenier
 */
public class TemplateAssignment extends BtrPlaceTree {

  private final NamingService<Node> namingServiceNodes;
  private final NamingService<VM> namingServiceVMs;

  private final Model mo;

  /**
   * The current script.
   */
  private final Script script;

  /**
   * The template factory.
   */
  private final TemplateFactory tpls;

    private final SymbolsTable syms;

  /**
     * Make a new tree.
     *
     * @param t            the token to consider
     * @param s            the script that is built
     * @param tplFactory    the template factory
     * @param symbolsTable the symbol table
     * @param m the model we focus on
     * @param nsNodes the NamingService for the nodes
     * @param nsVMs the NamingService for the VMs
     * @param errs         the errors
     */
    public TemplateAssignment(Token t, Script s, TemplateFactory tplFactory, SymbolsTable symbolsTable, Model m, NamingService<Node> nsNodes, NamingService<VM> nsVMs, ErrorReporter errs) {
        super(t, errs);
        this.script = s;
        this.tpls = tplFactory;
        this.syms = symbolsTable;
        this.mo = m;
        this.namingServiceNodes = nsNodes;
        this.namingServiceVMs = nsVMs;
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
            @SuppressWarnings("unchecked")
            List<BtrPlaceTree> children = (List<BtrPlaceTree>) t.getChildren();
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
            Element el = namingServiceVMs.resolve(id);
            if (el == null) {
                VM vm = mo.newVM();
                mo.getMapping().addReadyVM(vm);
                //We add the VM to the $me variable
                if (vm == null) {
                    ignoreError("No UUID to create node '" + id + "'");
                } else {
                    namingServiceVMs.register(vm, id);
                    el = vm;
                    ((BtrpSet) syms.getSymbol(SymbolsTable.ME)).getValues().add(
                            new BtrpElement(BtrpOperand.Type.VM, id, el));
                }
            }
            tpls.check(script, tplName, el, opts);
            if (!script.add(new BtrpElement(BtrpOperand.Type.VM, id, el))) {
                ignoreError("VM '" + id + "' already created");
            }
        } catch (ElementBuilderException ex) {
            ignoreError(ex);
        }
    }

    private void addNode(String tplName, String id, Map<String, String> opts) {
        try {
            Element el = namingServiceNodes.resolve(id);
            if (el == null) {
                Node n = mo.newNode();
                mo.getMapping().addOfflineNode(n);
                if (n == null) {
                    ignoreError("No UUID to create node '" + id + "'");
                } else {
                    namingServiceNodes.register(n, id);
                    el = n;
                }
            }
            tpls.check(script, tplName, el, opts);
            if (!script.add(new BtrpElement(BtrpOperand.Type.NODE, id, el))) {
                ignoreError("Node '" + id + "' already created");
            }
        } catch (ElementBuilderException ex) {
            ignoreError(ex);
        }
    }
}
