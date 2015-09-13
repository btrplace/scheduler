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
import org.btrplace.btrpsl.antlr.ANTLRBtrplaceSL2Parser;
import org.btrplace.btrpsl.element.BtrpElement;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.model.Element;
import org.btrplace.model.view.NamingService;

/**
 * A Tree parser to identify a virtual machine or a node.
 *
 * @author Fabien Hermenier
 */
public class ElementTree extends BtrPlaceTree {

    private Script script;

    private NamingService namingServiceNodes;
    private NamingService namingServiceVMs;

    /**
     * Make a new parser.
     *
     * @param t    the token to analyze
     * @param errs the errors to report
     */
    public ElementTree(Token t, NamingService nsNodes, NamingService nsVMs, Script scr, ErrorReporter errs) {
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
                String ref = lbl.substring(1, lbl.length());
                el = namingServiceNodes.resolve(lbl);
                if (el == null) {
                    return ignoreError("Unknown node '" + ref + "'");
                }
                btrpEl = new BtrpElement(BtrpOperand.Type.node, lbl, el);
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
