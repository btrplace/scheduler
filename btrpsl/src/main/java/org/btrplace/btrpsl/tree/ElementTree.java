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
import org.btrplace.btrpsl.element.BtrpElement;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.model.Element;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.NamingService;

/**
 * A Tree parser to identify a virtual machine or a node.
 *
 * @author Fabien Hermenier
 */
public class ElementTree extends BtrPlaceTree {

  private final Script script;

  private final NamingService<Node> namingServiceNodes;
  private final NamingService<VM> namingServiceVMs;

  /**
   * Make a new parser.
   *
   * @param t       the token to analyze
   * @param nsNodes the Naming Service for the nodes
   * @param nsVMs   the Naming Service for the VMs
   * @param scr     the script we analyse
   * @param errs    the errors to report
   */
    public ElementTree(Token t, NamingService<Node> nsNodes, NamingService<VM> nsVMs, Script scr, ErrorReporter errs) {
        super(t, errs);
        this.script = scr;
        this.namingServiceNodes = nsNodes;
        this.namingServiceVMs = nsVMs;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        String lbl = getText();
        Element el;
        BtrpElement btrpEl;
        switch (token.getType()) {
            case ANTLRBtrplaceSL2Parser.NODE_NAME:
                String ref = lbl.substring(1);
              el = namingServiceNodes.resolve(lbl);
                if (el == null) {
                    return ignoreError("Unknown node '" + ref + "'");
                }
                btrpEl = new BtrpElement(BtrpOperand.Type.NODE, lbl, el);
                break;
            case ANTLRBtrplaceSL2Parser.IDENTIFIER:
                /**
                 * Switch to Fully Qualified name before getting the VM
                 */
                String fqn = script.id() + '.' + lbl;
                el = namingServiceVMs.resolve(fqn);
                if (el == null) {
                    return ignoreError("Unknown VM '" + lbl + "'");
                }
                btrpEl = new BtrpElement(BtrpOperand.Type.VM, fqn, el);
                break;
            default:
                return ignoreError("Unexpected type: " + ANTLRBtrplaceSL2Parser.tokenNames[token.getType()]);
        }
        return btrpEl;
    }
}
